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
import de.fhdortmund.mystudyapp.identity.model.User;
import de.fhdortmund.mystudyapp.identity.repository.UserRepository;
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

    /* ==================== CRUD ==================== */

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
        log.info("RSVP reactivated: {} for event {} with status {}", saved.getId(), event.getId(), saved.getStatus());
        return rsvpMapper.toDto(saved);
    }

    @Transactional
    public RsvpDto cancelRsvp(UUID rsvpId, String userEmail) {
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
        Rsvp saved = rsvpRepository.save(rsvp);

        if (wasGoing) {
            eventRepository.decrementRsvpCount(rsvp.getEvent().getId());
            eventPublisher.publishRsvpCancelled(rsvp.getEvent().getId(), rsvpId);
        }

        log.info("RSVP cancelled: {} by {}", rsvpId, userEmail);
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

    /* ==================== Waitlist Position ==================== */

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

    /* ==================== Queries ==================== */

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

    /* ==================== Helpers ==================== */

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