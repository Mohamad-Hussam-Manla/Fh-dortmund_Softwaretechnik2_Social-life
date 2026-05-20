package de.fhdortmund.mystudyapp.notification.service;

import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.fhdortmund.mystudyapp.common.exception.ResourceNotFoundException;
import de.fhdortmund.mystudyapp.common.response.PageResponse;
import de.fhdortmund.mystudyapp.identity.model.User;
import de.fhdortmund.mystudyapp.identity.repository.UserRepository;
import de.fhdortmund.mystudyapp.notification.dto.NotificationDto;
import de.fhdortmund.mystudyapp.notification.mapper.NotificationMapper;
import de.fhdortmund.mystudyapp.notification.model.Notification;
import de.fhdortmund.mystudyapp.notification.model.NotificationType;
import de.fhdortmund.mystudyapp.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    /* ==================== CRUD ==================== */

    @Transactional(readOnly = true)
    public PageResponse<NotificationDto> getNotifications(String userEmail, boolean unreadOnly, Pageable pageable) {
        User user = findUser(userEmail);

        Page<Notification> page = unreadOnly
                ? notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(user.getId(), false, pageable)
                : notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);

        return buildPageResponse(page);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String userEmail) {
        User user = findUser(userEmail);
        return notificationRepository.countByUserIdAndIsRead(user.getId(), false);
    }

    @Transactional
    public void markAsRead(String userEmail, UUID notificationId) {
        User user = findUser(userEmail);

        Notification notification = notificationRepository.findByIdAndUserId(notificationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notification.setRead(true);
        notificationRepository.save(notification);
        log.debug("Notification {} marked as read by {}", notificationId, userEmail);
    }

    @Transactional
    public int markAllAsRead(String userEmail) {
        User user = findUser(userEmail);
        int updated = notificationRepository.markAllAsReadByUserId(user.getId());
        log.info("Marked {} notifications as read for {}", updated, userEmail);
        return updated;
    }

    @Transactional
    public void deleteNotification(String userEmail, UUID notificationId) {
        User user = findUser(userEmail);

        Notification notification = notificationRepository.findByIdAndUserId(notificationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notificationRepository.delete(notification);
        log.debug("Notification {} deleted by {}", notificationId, userEmail);
    }

    /* ==================== Creation (called by other services) ==================== */

    /**
     * Creates a notification for a specific user.
     * Called by EventService, RsvpService, ReviewService, TrustLevelService, etc.
     */
    @Transactional
    public void createNotification(UUID userId, NotificationType type, String title, String message,
                                    UUID relatedEventId, UUID relatedUserId, String actionUrl) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("Cannot create notification for non-existent user {}", userId);
            return;
        }

        // Simple deduplication: skip if same type+event already exists
        if (relatedEventId != null && notificationRepository.existsByUserIdAndTypeAndRelatedEventId(
                userId, type, relatedEventId)) {
            log.debug("Duplicate notification skipped: {} for user {} event {}", type, userId, relatedEventId);
            return;
        }

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .relatedEventId(relatedEventId)
                .relatedUserId(relatedUserId)
                .actionUrl(actionUrl)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("Notification created: {} for user {} (event: {})", type, userId, relatedEventId);
    }

    /** Convenience: notify by email lookup */
    @Transactional
    public void createNotification(String userEmail, NotificationType type, String title, String message,
                                    UUID relatedEventId, UUID relatedUserId, String actionUrl) {
        User user = userRepository.findByUniversityEmail(userEmail).orElse(null);
        if (user == null) {
            log.warn("Cannot create notification for non-existent email {}", userEmail);
            return;
        }
        createNotification(user.getId(), type, title, message, relatedEventId, relatedUserId, actionUrl);
    }

    /* ==================== Helpers ==================== */

    private User findUser(String email) {
        return userRepository.findByUniversityEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private PageResponse<NotificationDto> buildPageResponse(Page<Notification> page) {
        return PageResponse.<NotificationDto>builder()
                .content(page.getContent().stream()
                        .map(notificationMapper::toDto)
                        .collect(Collectors.toList()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}

