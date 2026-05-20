package de.fhdortmund.mystudyapp.notification.publisher;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import de.fhdortmund.mystudyapp.notification.model.NotificationType;
import lombok.RequiredArgsConstructor;

/**
 * PHASE 1.3: Central publisher for notification events.
 * Other services call this instead of directly injecting NotificationService,
 * keeping the notification system loosely coupled.
 */
@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publish(UUID userId, NotificationType type, String title, String message,
                        UUID relatedEventId, UUID relatedUserId, String actionUrl) {
        publisher.publishEvent(new NotificationEvent(this, userId, type, title, message,
                relatedEventId, relatedUserId, actionUrl));
    }

    /**
     * Convenience method for event-related notifications.
     */
    public void publishEventNotification(UUID userId, NotificationType type, String title, String message,
                                          UUID eventId, String eventTitle) {
        String actionUrl = "/events/" + eventId;
        publish(userId, type, title, message, eventId, null, actionUrl);
    }
}
