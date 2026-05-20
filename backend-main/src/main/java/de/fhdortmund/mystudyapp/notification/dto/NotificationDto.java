package de.fhdortmund.mystudyapp.notification.dto;

import java.time.Instant;
import java.util.UUID;

import de.fhdortmund.mystudyapp.notification.model.NotificationType;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for notification list items and detail views.
 */
@Data
@Builder
public class NotificationDto {
    private UUID id;
    private NotificationType type;
    private String title;
    private String message;
    private UUID relatedEventId;
    private UUID relatedUserId;
    private String actionUrl;
    private boolean isRead;
    private Instant createdAt;
}
