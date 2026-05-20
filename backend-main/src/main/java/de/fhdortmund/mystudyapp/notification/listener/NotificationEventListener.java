package de.fhdortmund.mystudyapp.notification.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import de.fhdortmund.mystudyapp.notification.publisher.NotificationEvent;
import de.fhdortmund.mystudyapp.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * PHASE 1.3: Listens for NotificationEvent and persists them.
 * Runs within the same transaction as the triggering operation by default.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @EventListener
    @Transactional
    public void handleNotificationEvent(NotificationEvent event) {
        notificationService.createNotification(
                event.getUserId(),
                event.getType(),
                event.getTitle(),
                event.getMessage(),
                event.getRelatedEventId(),
                event.getRelatedUserId(),
                event.getActionUrl()
        );
    }
}

