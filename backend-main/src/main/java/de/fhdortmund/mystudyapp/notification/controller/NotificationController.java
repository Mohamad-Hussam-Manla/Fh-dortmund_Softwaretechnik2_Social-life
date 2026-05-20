package de.fhdortmund.mystudyapp.notification.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.fhdortmund.mystudyapp.common.response.ApiResponse;
import de.fhdortmund.mystudyapp.common.response.PageResponse;
import de.fhdortmund.mystudyapp.notification.dto.NotificationDto;
import de.fhdortmund.mystudyapp.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;

/**
 * PHASE 1.3: Notification Endpoints
 *
 * GET    /api/notifications              → Paginated list
 * GET    /api/notifications/unread-count → Badge count
 * PATCH  /api/notifications/{id}/read    → Mark single read
 * PATCH  /api/notifications/read-all     → Mark all read
 * DELETE /api/notifications/{id}         → Dismiss
 */
@RestController
@RequestMapping("/api/notifications")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationDto>>> getNotifications(
            @RequestParam(required = false, defaultValue = "false") boolean unreadOnly,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal User principal) {

        PageResponse<NotificationDto> notifications = notificationService.getNotifications(
                principal.getUsername(), unreadOnly, pageable);

        return ResponseEntity.ok(ApiResponse.success(notifications, "Notifications retrieved"));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal User principal) {

        long count = notificationService.getUnreadCount(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(count, "Unread count retrieved"));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable UUID notificationId,
            @AuthenticationPrincipal User principal) {

        notificationService.markAsRead(principal.getUsername(), notificationId);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification marked as read"));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Integer>> markAllAsRead(
            @AuthenticationPrincipal User principal) {

        int updated = notificationService.markAllAsRead(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(updated, "All notifications marked as read"));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable UUID notificationId,
            @AuthenticationPrincipal User principal) {

        notificationService.deleteNotification(principal.getUsername(), notificationId);
        return ResponseEntity.ok(ApiResponse.success(null, "Notification deleted"));
    }
}

