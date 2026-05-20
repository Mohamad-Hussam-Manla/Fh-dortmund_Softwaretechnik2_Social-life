package de.fhdortmund.mystudyapp.registration.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.fhdortmund.mystudyapp.common.exception.ForbiddenActionException;
import de.fhdortmund.mystudyapp.common.exception.ResourceNotFoundException;
import de.fhdortmund.mystudyapp.common.response.PageResponse;
import de.fhdortmund.mystudyapp.events.model.Event;
import de.fhdortmund.mystudyapp.events.model.EventStatus;
import de.fhdortmund.mystudyapp.events.repository.EventRepository;
import de.fhdortmund.mystudyapp.events.service.EventSseService;
import de.fhdortmund.mystudyapp.identity.model.User;
import de.fhdortmund.mystudyapp.identity.repository.UserRepository;
import de.fhdortmund.mystudyapp.notification.model.NotificationType;
import de.fhdortmund.mystudyapp.notification.publisher.NotificationEventPublisher;
import de.fhdortmund.mystudyapp.registration.dto.RsvpDto;
import de.fhdortmund.mystudyapp.registration.mapper.RsvpMapper;
import de.fhdortmund.mystudyapp.registration.model.Rsvp;
import de.fhdortmund.mystudyapp.registration.model.RsvpStatus;
import de.fhdortmund.mystudyapp.registration.observer.RsvpEventPublisher;
import de.fhdortmund.mystudyapp.registration.repository.RsvpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RsvpService {

    private final RsvpRepository rsvpRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RsvpMapper rsvpMapper;
    private final RsvpEventPublisher eventPublisher;

    // PHASE 1.3: Notify host of RSVP cancellations
    private final NotificationEventPublisher notificationPublisher;

    // PHASE 1.4: Real-time RSVP count updates
    private final EventSseService sseService;

    @Transactional
    public RsvpDto createRsvp(UUID eventId, String userEmail) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new ForbiddenActionException("rsvp", "Event is not open for registration");
        }

        if (event.getEndTime().isBefore(Instant.now())) {
            throw new ForbiddenActionException("rsvp", "Cannot RSVP to an event that has already ended");
        }

        // PHASE 2: Cannot RSVP to deleted events
        if (event.getDeletedAt() != null) {
            throw new ForbiddenActionException("rsvp", "Event has been deleted");
        }

        Optional<Rsvp> existing = rsvpRepository.findByEventIdAndUserId(eventId, user.getId());
        if (existing.isPresent()) {
            return handleExistingRsvp(existing.get(), event, userEmail);
        }

        // Atomic increment: returns 1 if there was capacity, 0 if full
        int updated = eventRepository.incrementRsvpCount(eventId);
        RsvpStatus status = (updated > 0) ? RsvpStatus.GOING : RsvpStatus.WAITLISTED;

        Rsvp rsvp = Rsvp.builder()
                .event(event)
                .user(user)
                .status(status)
                .build();

        Rsvp saved = rsvpRepository.save(rsvp);

        // PHASE 1.4: Broadcast RSVP update to all event subscribers
        sseService.notifyRsvpUpdate(eventId, event.getCurrentRsvpCount() + (updated > 0 ? 1 : 0), event.getMaxCapacity());

        log.info("RSVP created: {} for event {} by {} with status {}", saved.getId(), eventId, userEmail, status);
        return rsvpMapper.toDto(saved);
    }

    private RsvpDto handleExistingRsvp(Rsvp existing, Event event, String userEmail) {
        if (existing.getStatus() == RsvpStatus.GOING || existing.getStatus() == RsvpStatus.WAITLISTED) {
            throw new ForbiddenActionException("rsvp", "You are already registered for this event");
        }

        if (existing.getStatus() == RsvpStatus.ATTENDED) {
            throw new ForbiddenActionException("rsvp", "You have already attended this event");
        }

        // Try to atomically claim a spot if capacity is available
        int updated = eventRepository.incrementRsvpCount(event.getId());
        if (updated > 0) {
            existing.setStatus(RsvpStatus.GOING);
        } else {
            existing.setStatus(RsvpStatus.WAITLISTED);
        }

        Rsvp saved = rsvpRepository.save(existing);

        // PHASE 1.4: Broadcast update
        sseService.notifyRsvpUpdate(event.getId(), event.getCurrentRsvpCount() + (updated > 0 ? 1 : 0), event.getMaxCapacity());

        log.info("RSVP reactivated: {} for event {} with status {}", saved.getId(), event.getId(), saved.getStatus());
        return rsvpMapper.toDto(saved);
    }

    @Transactional
    public RsvpDto cancelRsvp(UUID rsvpId, String userEmail, String reason) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Rsvp rsvp = rsvpRepository.findById(rsvpId)
                .orElseThrow(() -> new ResourceNotFoundException("RSVP", "id", rsvpId));

        if (!rsvp.getUser().getId().equals(user.getId())) {
            throw new ForbiddenActionException("cancel rsvp", "You can only cancel your own registration");
        }

        if (rsvp.getStatus() == RsvpStatus.CANCELLED) {
            throw new ForbiddenActionException("cancel rsvp", "Registration is already cancelled");
        }

        if (rsvp.getStatus() == RsvpStatus.ATTENDED) {
            throw new ForbiddenActionException("cancel rsvp", "Cannot cancel an attended registration");
        }

        boolean wasGoing = rsvp.getStatus() == RsvpStatus.GOING;
        rsvp.setStatus(RsvpStatus.CANCELLED);
        rsvp.setCancellationReason(reason);  // PHASE 2
        Rsvp saved = rsvpRepository.save(rsvp);

        if (wasGoing) {
            eventRepository.decrementRsvpCount(rsvp.getEvent().getId());
            eventPublisher.publishRsvpCancelled(rsvp.getEvent().getId(), rsvpId);

            // PHASE 1.3: Notify host that someone cancelled
            String message = reason != null && !reason.isBlank()
        ? user.getDisplayName() + " cancelled their RSVP for \"" + rsvp.getEvent().getTitle() + "\". Reason: " + reason
        : user.getDisplayName() + " cancelled their RSVP for \"" + rsvp.getEvent().getTitle() + "\".";

            notificationPublisher.publishEventNotification(
                rsvp.getEvent().getHost().getId(),
                NotificationType.RSVP_CANCELLED,
                "Attendee Cancelled",
                message,
                rsvp.getEvent().getId(),
                rsvp.getEvent().getTitle()
            );
        }

        // PHASE 1.4: Broadcast RSVP update (count decreased)
        Event event = rsvp.getEvent();
        sseService.notifyRsvpUpdate(event.getId(), Math.max(0, event.getCurrentRsvpCount() - 1), event.getMaxCapacity());

        log.info("RSVP cancelled: {} by {}. Reason: {}", rsvpId, userEmail, reason);
        return rsvpMapper.toDto(saved);
    }

    @Transactional
    public RsvpDto markAttended(UUID eventId, UUID rsvpId, String hostEmail) {
        User host = userRepository.findByUniversityEmail(hostEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (!event.getHost().getId().equals(host.getId())) {
            throw new ForbiddenActionException("mark attendance", "Only the host can mark attendance");
        }

        Rsvp rsvp = rsvpRepository.findById(rsvpId)
                .orElseThrow(() -> new ResourceNotFoundException("RSVP", "id", rsvpId));

        if (!rsvp.getEvent().getId().equals(eventId)) {
            throw new ForbiddenActionException("mark attendance", "RSVP does not belong to this event");
        }

        if (rsvp.getStatus() != RsvpStatus.GOING) {
            throw new ForbiddenActionException("mark attendance", "Only confirmed attendees can be marked as attended");
        }

        rsvp.setStatus(RsvpStatus.ATTENDED);
        Rsvp saved = rsvpRepository.save(rsvp);
        log.info("Attendance marked for RSVP {} at event {}", rsvpId, eventId);
        return rsvpMapper.toDto(saved);
    }

    /**
     * PHASE 2: Self check-in via QR code.
     */
    @Transactional
    public RsvpDto checkIn(UUID eventId, String code, String userEmail) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (event.getDeletedAt() != null) {
            throw new ForbiddenActionException("check-in", "Event has been deleted");
        }

        if (event.getCheckInCode() == null || !event.getCheckInCode().equalsIgnoreCase(code)) {
            throw new ForbiddenActionException("check-in", "Invalid check-in code");
        }

        Rsvp rsvp = rsvpRepository.findByEventIdAndUserId(eventId, user.getId())
                .orElseThrow(() -> new ForbiddenActionException("check-in", "You must RSVP to this event before checking in"));

        if (rsvp.getStatus() != RsvpStatus.GOING) {
            throw new ForbiddenActionException("check-in", "Only confirmed attendees can check in");
        }

        rsvp.setStatus(RsvpStatus.ATTENDED);
        Rsvp saved = rsvpRepository.save(rsvp);
        log.info("Self check-in: RSVP {} at event {} by {}", saved.getId(), eventId, userEmail);
        return rsvpMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public int getWaitlistPosition(UUID rsvpId, String userEmail) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Rsvp rsvp = rsvpRepository.findById(rsvpId)
                .orElseThrow(() -> new ResourceNotFoundException("RSVP", "id", rsvpId));

        if (!rsvp.getUser().getId().equals(user.getId())) {
            throw new ForbiddenActionException("view position", "You can only view your own waitlist position");
        }

        if (rsvp.getStatus() != RsvpStatus.WAITLISTED) {
            return 0;
        }

        long ahead = rsvpRepository.countByEventIdAndStatusAndCreatedAtLessThan(
                rsvp.getEvent().getId(), RsvpStatus.WAITLISTED, rsvp.getCreatedAt());

        return (int) ahead + 1;
    }

    @Transactional(readOnly = true)
    public RsvpDto getMyRsvpForEvent(UUID eventId, String userEmail) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Rsvp rsvp = rsvpRepository.findByEventIdAndUserId(eventId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("RSVP not found"));

        return rsvpMapper.toDto(rsvp);
    }

    @Transactional(readOnly = true)
    public PageResponse<RsvpDto> getMyRsvps(String userEmail, Pageable pageable) {
        User user = userRepository.findByUniversityEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<Rsvp> page = rsvpRepository.findByUserId(user.getId(), pageable);
        return buildPageResponse(page);
    }

    @Transactional(readOnly = true)
    public PageResponse<RsvpDto> getEventRsvps(UUID eventId, String hostEmail, Pageable pageable) {
        User host = userRepository.findByUniversityEmail(hostEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (!event.getHost().getId().equals(host.getId())) {
            throw new ForbiddenActionException("view registrations", "Only the host can view event registrations");
        }

        Page<Rsvp> page = rsvpRepository.findByEventId(eventId, pageable);
        return buildPageResponse(page);
    }

    @Transactional(readOnly = true)
    public PageResponse<RsvpDto> getEventRsvpsByStatus(UUID eventId, RsvpStatus status, String hostEmail, Pageable pageable) {
        User host = userRepository.findByUniversityEmail(hostEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (!event.getHost().getId().equals(host.getId())) {
            throw new ForbiddenActionException("view registrations", "Only the host can view event registrations");
        }

        Page<Rsvp> page = rsvpRepository.findByEventIdAndStatus(eventId, status, pageable);
        return buildPageResponse(page);
    }

    private PageResponse<RsvpDto> buildPageResponse(Page<Rsvp> page) {
        return PageResponse.<RsvpDto>builder()
                .content(page.getContent().stream()
                        .map(rsvpMapper::toDto)
                        .collect(Collectors.toList()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}