package de.fhdortmund.mystudyapp.events.mapper;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import de.fhdortmund.mystudyapp.events.dto.CategoryDto;
import de.fhdortmund.mystudyapp.events.dto.EventDto;
import de.fhdortmund.mystudyapp.events.dto.EventMediaDto;
import de.fhdortmund.mystudyapp.events.dto.HostDto;
import de.fhdortmund.mystudyapp.events.model.Event;
import de.fhdortmund.mystudyapp.events.model.EventMedia;
import de.fhdortmund.mystudyapp.events.repository.EventRepository;
import de.fhdortmund.mystudyapp.identity.mapper.UserMapper;
import de.fhdortmund.mystudyapp.moderation.repository.ReviewRepository;
import de.fhdortmund.mystudyapp.registration.model.Rsvp;
import de.fhdortmund.mystudyapp.registration.repository.RsvpRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EventMapper {

    private final UserMapper userMapper;
    private final RsvpRepository rsvpRepository;

    // PHASE 0: Injected to fetch host aggregates without N+1
    private final EventRepository eventRepository;
    private final ReviewRepository reviewRepository;

    public EventDto toDto(Event event, UUID currentUserId) {
        if (event == null) return null;

        Set<CategoryDto> categories = event.getEventCategories().stream()
                .map(ec -> CategoryDto.builder()
                        .id(ec.getCategory().getId())
                        .name(ec.getCategory().getName())
                        .icon(ec.getCategory().getIcon())          // PHASE 2
                        .color(ec.getCategory().getColor())        // PHASE 2
                        .sortOrder(ec.getCategory().getSortOrder()) // PHASE 2
                        .build())
                .collect(Collectors.toSet());

        boolean isHost = currentUserId != null && event.getHost() != null
                && currentUserId.equals(event.getHost().getId());

        Rsvp myRsvp = null;
        if (currentUserId != null) {
            myRsvp = rsvpRepository.findByEventIdAndUserId(event.getId(), currentUserId).orElse(null);
        }

        // PHASE 0 FIX: Build HostDto with embedded aggregates
        HostDto hostDto = buildHostDto(event.getHost());

        return EventDto.builder()
                .id(event.getId())
                .host(hostDto)                              // ← was userMapper.toDto(...)
                .title(event.getTitle())
                .description(event.getDescription())
                .location(event.getLocation())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .maxCapacity(event.getMaxCapacity())
                .currentRsvpCount(event.getCurrentRsvpCount())
                .status(event.getStatus())
                .categories(categories)
                .media(mapMedia(event))
                .createdAt(event.getCreatedAt())
                .isHost(isHost)
                .myRsvpStatus(myRsvp != null ? myRsvp.getStatus() : null)
                // PHASE 2 ADDITIONS
                .slug(event.getSlug())
                .viewCount(event.getViewCount())
                .cancellationReason(event.getCancellationReason())
                .build();
    }

    public EventDto toDto(Event event) {
        return toDto(event, null);
    }

    /**
     * PHASE 0: Builds a HostDto with trust-level aggregates.
     * Uses existing repository queries so no new DB schema is required.
     */
    private HostDto buildHostDto(de.fhdortmund.mystudyapp.identity.model.User host) {
        if (host == null) return null;

        UUID hostId = host.getId();

        Long completedEventsWithReviews = eventRepository.countCompletedReviewedEventsByHostId(hostId);
        Double averageHostRating = reviewRepository.calculateAverageRatingByHostId(hostId);
        Long totalHostReviews = reviewRepository.countTotalReviewsByHostId(hostId);

        return HostDto.builder()
                .id(hostId)
                .displayName(host.getDisplayName())
                .profileImageUrl(host.getProfileImageUrl())
                .trustLevel(host.getTrustLevel())
                .averageHostRating(averageHostRating != null ? averageHostRating : 0.0)
                .totalHostReviews(totalHostReviews != null ? totalHostReviews : 0L)
                .completedEventsWithReviews(completedEventsWithReviews != null ? completedEventsWithReviews : 0L)
                .build();
    }

    private List<EventMediaDto> mapMedia(Event event) {
        if (event.getEventMedia() == null) return Collections.emptyList();
        return event.getEventMedia().stream()
                .sorted(Comparator.comparing(EventMedia::getDisplayOrder)  // PHASE 2: sort by displayOrder
                        .thenComparing(EventMedia::getCreatedAt))          // fallback to createdAt
                .map(m -> EventMediaDto.builder()
                        .id(m.getId())
                        .url(m.getUrl())
                        .mediaType(m.getMediaType())
                        .filename(m.getFilename())
                        .thumbnailUrl(m.getThumbnailUrl())    // PHASE 2
                        .mediumUrl(m.getMediumUrl())          // PHASE 2
                        .displayOrder(m.getDisplayOrder())    // PHASE 2
                        .build())
                .collect(Collectors.toList());
    }
}