package de.fhdortmund.mystudyapp.notification.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.fhdortmund.mystudyapp.notification.model.Notification;
import de.fhdortmund.mystudyapp.notification.model.NotificationType;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /** Paginated list of notifications for a user, newest first */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /** Filtered by read status */
    Page<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(
            UUID userId, boolean isRead, Pageable pageable);

    /** Count unread notifications for badge */
    long countByUserIdAndIsRead(UUID userId, boolean isRead);

    /** Find single notification ensuring it belongs to the user */
    Optional<Notification> findByIdAndUserId(UUID id, UUID userId);

    /** Mark all notifications as read for a user */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") UUID userId);

    /** Find recent unread notifications (for real-time polling fallback) */
    List<Notification> findTop10ByUserIdAndIsReadOrderByCreatedAtDesc(UUID userId, boolean isRead);

    /** Check if a similar notification already exists (deduplication) */
    boolean existsByUserIdAndTypeAndRelatedEventId(
            UUID userId, NotificationType type, UUID relatedEventId);
}
