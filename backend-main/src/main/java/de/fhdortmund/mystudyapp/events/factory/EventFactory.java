package de.fhdortmund.mystudyapp.events.factory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import de.fhdortmund.mystudyapp.events.dto.CreateEventRequest;
import de.fhdortmund.mystudyapp.events.model.Event;
import de.fhdortmund.mystudyapp.events.model.EventStatus;
import de.fhdortmund.mystudyapp.events.service.SlugGenerator;
import de.fhdortmund.mystudyapp.identity.model.Role;
import de.fhdortmund.mystudyapp.identity.model.TrustLevel;
import de.fhdortmund.mystudyapp.identity.model.User;
import de.fhdortmund.mystudyapp.identity.repository.UserRepository;
import de.fhdortmund.mystudyapp.mqtt.dto.OfficialEventMessage;
import lombok.RequiredArgsConstructor;

/**
 * CREATIONAL PATTERN: Factory
 * Centralizes creation of Event entities from multiple sources (REST API, MQTT, etc.)
 * ensuring consistent defaults and business rules.
 */
@Component
@RequiredArgsConstructor
public class EventFactory {

    private static final DateTimeFormatter ASTA_TIME_FORMAT = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final UserRepository userRepository;
    private final SlugGenerator slugGenerator;

    /* ==================== REST API Events ==================== */

    public Event createEvent(CreateEventRequest request, User host) {
        EventStatus initialStatus = (host.getTrustLevel() == TrustLevel.TRUSTED_HOST
                || host.getRole() == Role.ADMIN)
                ? EventStatus.PUBLISHED
                : EventStatus.UNDER_REVIEW;

        String slug = request.getSlug() != null && !request.getSlug().isBlank()
                ? request.getSlug()
                : slugGenerator.generateSlug(request.getTitle());

        return Event.builder()
                .host(host)
                .title(request.getTitle().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .location(request.getLocation().trim())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .maxCapacity(request.getMaxCapacity())
                .currentRsvpCount(0)
                .status(initialStatus)
                .slug(slug)
                .viewCount(0L)
                .build();
    }

    /**
     * PHASE 2: Create a draft event — bypasses trust level checks and date validation.
     * Drafts are private to the host until published.
     */
    public Event createDraft(CreateEventRequest request, User host) {
        String slug = request.getSlug() != null && !request.getSlug().isBlank()
                ? request.getSlug()
                : slugGenerator.generateSlug(request.getTitle());

        return Event.builder()
                .host(host)
                .title(request.getTitle().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .location(request.getLocation() != null ? request.getLocation().trim() : "TBD")
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .maxCapacity(request.getMaxCapacity() != null ? request.getMaxCapacity() : 10)
                .currentRsvpCount(0)
                .status(EventStatus.DRAFT)
                .slug(slug)
                .viewCount(0L)
                .build();
    }

    /* ==================== MQTT/AStA Official Events ==================== */

    /**
     * Creates an Event from an AStA MQTT message.
     * Official events are always PUBLISHED and use a synthetic AStA host.
     */
    public Event createOfficialEvent(OfficialEventMessage message) {
        LocalDateTime startLocal = LocalDateTime.parse(message.getTime(), ASTA_TIME_FORMAT);
        Instant startTime = startLocal.atZone(ZoneId.of("Europe/Berlin")).toInstant();
        Instant endTime = startTime.plusSeconds(7200); // Default 2-hour duration

        User astaHost = getOrCreateAstaHost();

        return Event.builder()
                .host(astaHost)
                .title(message.getActivityName().trim())
                .description("Official AStA event: " + message.getActivityName())
                .location(message.getVenue().trim())
                .startTime(startTime)
                .endTime(endTime)
                .maxCapacity(100) // Default for official events
                .currentRsvpCount(0)
                .status(EventStatus.PUBLISHED) // Official events skip review
                .slug(slugGenerator.generateSlug(message.getActivityName()))
                .viewCount(0L)
                .build();
    }

    private User getOrCreateAstaHost() {
        return userRepository.findByUniversityEmail("asta@fh-dortmund.de")
                .orElseGet(() -> {
                    User newHost = User.builder()
                            .universityEmail("asta@fh-dortmund.de")
                            .displayName("AStA Official")
                            .passwordHash("system-generated-no-login-allowed")
                            .role(Role.ADMIN)
                            .trustLevel(TrustLevel.TRUSTED_HOST)
                            .isVerified(true)
                            .build();
                    return userRepository.save(newHost);
                });
    }
}