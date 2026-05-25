package de.fhdortmund.mystudyapp.identity.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrustQualificationStatus {
    private final long completedEventsWithReviews;
    private final int minimumEventsRequired;
    private final double averageRating;
    private final double minimumRatingRequired;
    private final boolean meetsEventCount;
    private final boolean meetsRatingThreshold;
    private final boolean qualifies;
}