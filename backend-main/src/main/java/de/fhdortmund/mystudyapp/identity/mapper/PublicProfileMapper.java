package de.fhdortmund.mystudyapp.identity.mapper;

import org.springframework.stereotype.Component;

import de.fhdortmund.mystudyapp.identity.dto.PublicProfileDto;
import de.fhdortmund.mystudyapp.identity.model.User;

@Component
public class PublicProfileMapper {

    /**
     * Converts User to PublicProfileDto with trust qualification metrics.
     * 
     * @param user The user entity
     * @param completedEventsWithReviews Count of COMPLETED events that have at least one review
     * @param averageHostRating Average rating across all reviews for completed events
     * @return PublicProfileDto with trust metrics
     */
    public PublicProfileDto toDto(User user, Long completedEventsWithReviews, Double averageHostRating) {
        if (user == null) {
            return null;
        }

        return PublicProfileDto.builder()
                .id(user.getId())
                .displayName(user.getDisplayName())
                .bio(user.getBio())
                .profileImageUrl(user.getProfileImageUrl())
                .trustLevel(user.getTrustLevel())
                .createdAt(user.getCreatedAt())
                .completedEventsWithReviews(completedEventsWithReviews != null ? completedEventsWithReviews : 0L)
                .averageHostRating(averageHostRating != null ? averageHostRating : 0.0)
                .build();
    }
    
    /**
     * Creates a minimal public profile without metrics (for users with no events)
     */
    public PublicProfileDto toMinimalDto(User user) {
        if (user == null) {
            return null;
        }
        
        return PublicProfileDto.builder()
                .id(user.getId())
                .displayName(user.getDisplayName())
                .bio(user.getBio())
                .profileImageUrl(user.getProfileImageUrl())
                .trustLevel(user.getTrustLevel())
                .createdAt(user.getCreatedAt())
                .completedEventsWithReviews(0L)
                .averageHostRating(0.0)
                .build();
    }
}