package de.fhdortmund.mystudyapp.registration.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.fhdortmund.mystudyapp.common.exception.ForbiddenActionException;
import de.fhdortmund.mystudyapp.common.exception.ResourceNotFoundException;
import de.fhdortmund.mystudyapp.events.model.Event;
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
import de.fhdortmund.mystudyapp.registration.repository.RsvpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaitlistService {

    private final RsvpRepository rsvpRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RsvpMapper rsvpMapper;

    // PHASE 1.3: Notify promoted users
    private final NotificationEventPublisher notificationPublisher;

    // PHASE 1.4: Real-time waitlist updates
    private final EventSseService sseService;

    @Transactional
    public void promoteNextWaitlistedUser(UUID eventId) {
        // Pessimistic lock prevents concurrent promotions from overshooting capacity
        Event event = eventRepository.findByIdLocked(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (event.getCurrentRsvpCount() >= event.getMaxCapacity()) {
            log.debug("Event {} is at full capacity, no waitlist promotion possible", eventId);
            return;
        }

        Optional<Rsvp> nextWaitlisted = rsvpRepository
                .findFirstByEventIdAndStatusOrderByCreatedAtAsc(eventId, RsvpStatus.WAITLISTED);

        nextWaitlisted.ifPresent(rsvp -> promoteRsvp(rsvp, event));
    }

    /**
     * PHASE 2: Host manually promotes a specific waitlisted user.
     */
    @Transactional
    public RsvpDto promoteWaitlistedUser(UUID eventId, UUID rsvpId, String hostEmail) {
        User host = userRepository.findByUniversityEmail(hostEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findByIdLocked(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (!event.getHost().getId().equals(host.getId())) {
            throw new ForbiddenActionException("promote", "Only the host can promote waitlisted users");
        }

        Rsvp rsvp = rsvpRepository.findById(rsvpId)
                .orElseThrow(() -> new ResourceNotFoundException("RSVP", "id", rsvpId));

        if (!rsvp.getEvent().getId().equals(eventId)) {
            throw new ForbiddenActionException("promote", "RSVP does not belong to this event");
        }

        if (rsvp.getStatus() != RsvpStatus.WAITLISTED) {
            throw new ForbiddenActionException("promote", "Only waitlisted users can be promoted");
        }

        if (event.getCurrentRsvpCount() >= event.getMaxCapacity()) {
            throw new ForbiddenActionException("promote", "Event is at full capacity");
        }

        promoteRsvp(rsvp, event);
        log.info("Host {} manually promoted RSVP {} to GOING for event {}", hostEmail, rsvpId, eventId);
        return rsvpMapper.toDto(rsvp);
    }

    private void promoteRsvp(Rsvp rsvp, Event event) {
        rsvp.setStatus(RsvpStatus.GOING);
        rsvpRepository.save(rsvp);

        int newCount = event.getCurrentRsvpCount() + 1;
        event.setCurrentRsvpCount(newCount);
        eventRepository.save(event);

        // PHASE 1.3: Notify the promoted user
        notificationPublisher.publishEventNotification(
            rsvp.getUser().getId(),
            NotificationType.WAITLIST_PROMOTED,
            "You're In!",
            "A spot opened up for \"" + event.getTitle() + "\". You've been promoted from the waitlist!",
            event.getId(),
            event.getTitle()
        );
        // PHASE 1.4: Broadcast RSVP update + waitlist update
        sseService.notifyRsvpUpdate(event.getId(), newCount, event.getMaxCapacity());

        long remainingWaitlist = rsvpRepository.countByEventIdAndStatus(event.getId(), RsvpStatus.WAITLISTED);
        sseService.notifyWaitlistUpdate(event.getId(), (int) remainingWaitlist);

        log.info("Promoted waitlisted RSVP {} to GOING for event {}. Remaining waitlist: {}",
                rsvp.getId(), event.getId(), remainingWaitlist);
    }
}