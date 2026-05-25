package de.fhdortmund.mystudyapp.identity.dto;

import java.time.Instant;
import java.util.UUID;

import de.fhdortmund.mystudyapp.identity.model.TrustLevel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublicProfileDto {
    private UUID id;
    private String displayName;
    private String bio;
    private String profileImageUrl;
    private TrustLevel trustLevel;
    private Instant createdAt;
    
    // Renamed to be explicit: these are COMPLETED events with reviews
    private Long completedEventsWithReviews;  // Formerly hostedEventsCount
    private Double averageHostRating;
    

}