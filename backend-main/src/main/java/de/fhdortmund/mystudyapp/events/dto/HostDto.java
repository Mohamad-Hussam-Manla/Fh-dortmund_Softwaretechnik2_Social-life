package de.fhdortmund.mystudyapp.events.dto;

import java.util.UUID;

import de.fhdortmund.mystudyapp.identity.model.TrustLevel;
import lombok.Builder;
import lombok.Data;

/**
 * Enriched host representation for event cards and detail views.
 * Includes trust-level aggregates so the frontend never needs an N+1 call
 * to /api/public/users/{hostId} just to render stars.
 */
@Data
@Builder
public class HostDto {
    private UUID id;
    private String displayName;
    private String profileImageUrl;
    private TrustLevel trustLevel;

    /** Average rating across ALL reviews for this host's completed events (0.0 if none) */
    private Double averageHostRating;

    /** Total number of reviews left on this host's events */
    private Long totalHostReviews;

    /** Number of completed events that have at least one review */
    private Long completedEventsWithReviews;
}