package de.fhdortmund.mystudyapp.registration.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.fhdortmund.mystudyapp.common.exception.ResourceNotFoundException;
import de.fhdortmund.mystudyapp.events.model.Event;
import de.fhdortmund.mystudyapp.events.repository.EventRepository;
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

        nextWaitlisted.ifPresent(rsvp -> {
            rsvp.setStatus(RsvpStatus.GOING);
            rsvpRepository.save(rsvp);

            event.setCurrentRsvpCount(event.getCurrentRsvpCount() + 1);
            eventRepository.save(event);

            log.info("Promoted waitlisted RSVP {} to GOING for event {}", rsvp.getId(), eventId);
        });
    }
}