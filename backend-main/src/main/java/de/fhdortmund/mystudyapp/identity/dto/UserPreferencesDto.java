package de.fhdortmund.mystudyapp.identity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PHASE 3: DTO for user preferences / settings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferencesDto {

    private boolean emailNotifications;
    private boolean pushNotifications;
    private boolean notifyOnRsvpChange;
    private boolean notifyOnReview;
    private String timezone;
    private String language;
}