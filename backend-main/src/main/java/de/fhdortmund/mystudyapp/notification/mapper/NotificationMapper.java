package de.fhdortmund.mystudyapp.notification.mapper;

import org.springframework.stereotype.Component;

import de.fhdortmund.mystudyapp.notification.dto.NotificationDto;
import de.fhdortmund.mystudyapp.notification.model.Notification;

@Component
public class NotificationMapper {

    public NotificationDto toDto(Notification notification) {
        if (notification == null) return null;

        return NotificationDto.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .relatedEventId(notification.getRelatedEventId())
                .relatedUserId(notification.getRelatedUserId())
                .actionUrl(notification.getActionUrl())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}

