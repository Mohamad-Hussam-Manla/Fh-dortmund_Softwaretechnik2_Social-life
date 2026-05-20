package de.fhdortmund.mystudyapp.events.dto;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import de.fhdortmund.mystudyapp.events.model.EventStatus;
import de.fhdortmund.mystudyapp.registration.model.RsvpStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventDto {
    private UUID id;
    private HostDto host;
    private String title;
    private String description;
    private String location;
    private Instant startTime;
    private Instant endTime;
    private Integer maxCapacity;
    private Integer currentRsvpCount;
    private EventStatus status;
    private Set<CategoryDto> categories;
    private List<EventMediaDto> media;
    private Instant createdAt;
    private boolean isHost;
    /** Current user's RSVP status for this event (null if not registered) */
    private RsvpStatus myRsvpStatus;

    /* ==================== PHASE 2 ADDITIONS ==================== */

    /** Human-readable URL slug for sharing */
    private String slug;

    /** Social proof: how many times this event was viewed */
    private Long viewCount;

    /** Reason provided when host cancelled the event */
    private String cancellationReason;
}