package de.fhdortmund.mystudyapp.events.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.fhdortmund.mystudyapp.common.exception.ResourceNotFoundException;
import de.fhdortmund.mystudyapp.common.response.ApiResponse;
import de.fhdortmund.mystudyapp.common.response.PageResponse;
import de.fhdortmund.mystudyapp.events.dto.BulkEventActionRequest;
import de.fhdortmund.mystudyapp.events.dto.BulkEventActionResult;
import de.fhdortmund.mystudyapp.events.dto.EventDto;
import de.fhdortmund.mystudyapp.events.mapper.EventMapper;
import de.fhdortmund.mystudyapp.events.model.Event;
import de.fhdortmund.mystudyapp.events.model.EventStatus;
import de.fhdortmund.mystudyapp.events.repository.EventRepository;
import de.fhdortmund.mystudyapp.identity.repository.UserRepository;
import de.fhdortmund.mystudyapp.notification.model.NotificationType;
import de.fhdortmund.mystudyapp.notification.publisher.NotificationEventPublisher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/events")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminEventController {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;

    // PHASE 1.3: For sending notifications to hosts
    private final NotificationEventPublisher notificationPublisher;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<EventDto>>> getAllEvents(
            @RequestParam(required = false) EventStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal User principal) {

        Page<Event> page = (status != null)
                ? eventRepository.findByStatus(status, pageable)
                : eventRepository.findAll(pageable);

        UUID adminId = userRepository.findByUniversityEmail(principal.getUsername())
                .map(de.fhdortmund.mystudyapp.identity.model.User::getId)
                .orElse(null);

        PageResponse<EventDto> response = PageResponse.<EventDto>builder()
                .content(page.getContent().stream()
                        .map(e -> eventMapper.toDto(e, adminId))
                        .collect(Collectors.toList()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Events retrieved"));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<PageResponse<EventDto>>> getPendingEvents(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal User principal) {

        Page<Event> page = eventRepository.findByStatus(EventStatus.UNDER_REVIEW, pageable);

        UUID adminId = userRepository.findByUniversityEmail(principal.getUsername())
                .map(de.fhdortmund.mystudyapp.identity.model.User::getId)
                .orElse(null);

        PageResponse<EventDto> response = PageResponse.<EventDto>builder()
                .content(page.getContent().stream()
                        .map(e -> eventMapper.toDto(e, adminId))
                        .collect(Collectors.toList()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Pending events retrieved"));
    }

    @PatchMapping("/{eventId}/approve")
    public ResponseEntity<ApiResponse<EventDto>> approveEvent(
            @PathVariable UUID eventId,
            @AuthenticationPrincipal User principal) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        event.setStatus(EventStatus.PUBLISHED);
        Event saved = eventRepository.save(event);

        // PHASE 1.3: Notify host that their event was approved
        notificationPublisher.publishEventNotification(
        event.getHost().getId(),
        NotificationType.EVENT_APPROVED,
        "Event Approved",
        "Your event \"" + event.getTitle() + "\" has been approved and is now live!",
        event.getId(),
        event.getTitle()
        );

        UUID adminId = userRepository.findByUniversityEmail(principal.getUsername())
                .map(de.fhdortmund.mystudyapp.identity.model.User::getId)
                .orElse(null);

        return ResponseEntity.ok(ApiResponse.success(
                eventMapper.toDto(saved, adminId),
                "Event approved and published"));
    }

    @PatchMapping("/{eventId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectEvent(
            @PathVariable UUID eventId,
            @AuthenticationPrincipal User principal) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);

        // PHASE 1.3: Notify host that their event was rejected
        notificationPublisher.publishEventNotification(
                event.getHost().getId(),
                NotificationType.EVENT_REJECTED,
                "Event Rejected",
                "Your event '" + event.getTitle() + "' was not approved. Please review our guidelines.",
                event.getId(),
                event.getTitle()
        );

        return ResponseEntity.ok(ApiResponse.success(null, "Event rejected"));
    }

    @PatchMapping("/{eventId}/flag")
    public ResponseEntity<ApiResponse<EventDto>> flagEvent(
            @PathVariable UUID eventId,
            @AuthenticationPrincipal User principal) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        event.setStatus(EventStatus.UNDER_REVIEW);
        Event saved = eventRepository.save(event);

        UUID adminId = userRepository.findByUniversityEmail(principal.getUsername())
                .map(de.fhdortmund.mystudyapp.identity.model.User::getId)
                .orElse(null);

        return ResponseEntity.ok(ApiResponse.success(
                eventMapper.toDto(saved, adminId),
                "Event flagged for review"));
    }

    /* ==================== PHASE 2: BULK OPERATIONS ==================== */

    @PostMapping("/bulk-approve")
    public ResponseEntity<ApiResponse<BulkEventActionResult>> bulkApproveEvents(
            @Valid @RequestBody BulkEventActionRequest request,
            @AuthenticationPrincipal User principal) {

        BulkEventActionResult result = processBulkAction(request.getEventIds(), true, null, principal);
        return ResponseEntity.ok(ApiResponse.success(result, "Bulk approve completed"));
    }

    @PostMapping("/bulk-reject")
    public ResponseEntity<ApiResponse<BulkEventActionResult>> bulkRejectEvents(
            @Valid @RequestBody BulkEventActionRequest request,
            @AuthenticationPrincipal User principal) {

        BulkEventActionResult result = processBulkAction(request.getEventIds(), false, request.getReason(), principal);
        return ResponseEntity.ok(ApiResponse.success(result, "Bulk reject completed"));
    }

    private BulkEventActionResult processBulkAction(List<UUID> eventIds, boolean approve, String reason, User principal) {
        int successCount = 0;
        int failedCount = 0;
        java.util.List<UUID> succeededIds = new java.util.ArrayList<>();
        java.util.List<UUID> failedIds = new java.util.ArrayList<>();

        for (UUID eventId : eventIds) {
            try {
                Event event = eventRepository.findById(eventId)
                        .orElse(null);
                if (event == null) {
                    failedIds.add(eventId);
                    failedCount++;
                    continue;
                }

                if (approve) {
                    event.setStatus(EventStatus.PUBLISHED);
                    eventRepository.save(event);
                        notificationPublisher.publishEventNotification(
                        event.getHost().getId(),
                        NotificationType.EVENT_APPROVED,
                        "Event Approved",
                        "Your event \"" + event.getTitle() + "\" has been approved and is now live!",
                        event.getId(),
                        event.getTitle()
                        );
                } else {
                    event.setStatus(EventStatus.CANCELLED);
                    event.setCancellationReason(reason);
                    eventRepository.save(event);
                    notificationPublisher.publishEventNotification(
                            event.getHost().getId(),
                            NotificationType.EVENT_REJECTED,
                            "Event Rejected",
                            "Your event '" + event.getTitle() + "' was not approved. " +
                                    (reason != null ? "Reason: " + reason : "Please review our guidelines."),
                            event.getId(),
                            event.getTitle()
                    );
                }

                succeededIds.add(eventId);
                successCount++;

            } catch (Exception e) {
                failedIds.add(eventId);
                failedCount++;
            }
        }

        return BulkEventActionResult.builder()
                .processedCount(eventIds.size())
                .successCount(successCount)
                .failedCount(failedCount)
                .succeededIds(succeededIds)
                .failedIds(failedIds)
                .message(String.format("Processed %d events: %d succeeded, %d failed",
                        eventIds.size(), successCount, failedCount))
                .build();
    }
}