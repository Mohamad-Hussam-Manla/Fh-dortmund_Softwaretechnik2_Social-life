package de.fhdortmund.mystudyapp.notification.publisher;

import java.util.UUID;

import org.springframework.context.ApplicationEvent;

import de.fhdortmund.mystudyapp.notification.model.NotificationType;
import lombok.Getter;

/**
 * Spring ApplicationEvent carrying notification data.
 * Consumed by NotificationEventListener to persist notifications.
 */
@Getter
public class NotificationEvent extends ApplicationEvent {

    private final UUID userId;
    private final NotificationType type;
    private final String title;
    private final String message;
    private final UUID relatedEventId;
    private final UUID relatedUserId;
    private final String actionUrl;

    public NotificationEvent(Object source, UUID userId, NotificationType type, String title, String message,
                             UUID relatedEventId, UUID relatedUserId, String actionUrl) {
        super(source);
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.relatedEventId = relatedEventId;
        this.relatedUserId = relatedUserId;
        this.actionUrl = actionUrl;
    }
}

