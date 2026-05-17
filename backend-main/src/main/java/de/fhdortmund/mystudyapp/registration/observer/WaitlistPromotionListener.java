package de.fhdortmund.mystudyapp.registration.observer;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import de.fhdortmund.mystudyapp.registration.service.WaitlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaitlistPromotionListener {

    private final WaitlistService waitlistService;

    @EventListener
    @Transactional
    public void handleRsvpCancelled(RsvpCancelledEvent event) {
        log.debug("Handling RSVP cancellation for event {}, promoting next waitlisted user if possible",
                event.getEventId());
        waitlistService.promoteNextWaitlistedUser(event.getEventId());
    }
}