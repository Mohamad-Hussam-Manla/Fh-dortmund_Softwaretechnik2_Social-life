package de.fhdortmund.mystudyapp.mqtt.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import de.fhdortmund.mystudyapp.events.model.Event;
import de.fhdortmund.mystudyapp.events.model.EventStatus;
import de.fhdortmund.mystudyapp.identity.model.Role;
import de.fhdortmund.mystudyapp.identity.model.TrustLevel;
import de.fhdortmund.mystudyapp.identity.model.User;
import de.fhdortmund.mystudyapp.identity.repository.UserRepository;
import de.fhdortmund.mystudyapp.mqtt.adapter.EventMessageTarget;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OfficialEventAdapter implements EventMessageTarget {

    private static final DateTimeFormatter ASTA_TIME_FORMAT = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
    // Inject the UserRepository to talk to the database
    private final UserRepository userRepository;

    @Override
    public Event adapt(OfficialEventMessage message) {
        LocalDateTime startLocal = LocalDateTime.parse(message.getTime(), ASTA_TIME_FORMAT);
        Instant startTime = startLocal.atZone(ZoneId.of("Europe/Berlin")).toInstant();
        Instant endTime = startTime.plusSeconds(7200); 

        // Fetch or create the managed user from the database securely
        User astaHost = getOrCreateAstaHost();

        return Event.builder()
                .host(astaHost)
                .title(message.getActivityName())
                .description("Official AStA event: " + message.getActivityName())
                .location(message.getVenue())
                .startTime(startTime)
                .endTime(endTime)
                .maxCapacity(100) 
                .currentRsvpCount(0)
                .status(EventStatus.PUBLISHED) 
                .build();
    }

    private User getOrCreateAstaHost() {
        return userRepository.findByUniversityEmail("asta@fh-dortmund.de")
                .orElseGet(() -> {
                    User newHost = User.builder()
                            .universityEmail("asta@fh-dortmund.de")
                            .displayName("AStA Official")
                            // Required by your DB schema, even for system accounts
                            .passwordHash("system-generated-no-login-allowed") 
                            .role(Role.ADMIN)
                            .trustLevel(TrustLevel.TRUSTED_HOST)
                            .isVerified(true)
                            .build();
                    return userRepository.save(newHost); // Saves to DB before assigning to Event
                });
    }
}