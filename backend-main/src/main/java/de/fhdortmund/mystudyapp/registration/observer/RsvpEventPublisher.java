package de.fhdortmund.mystudyapp.registration.observer;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RsvpEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publishRsvpCancelled(UUID eventId, UUID rsvpId) {
        publisher.publishEvent(new RsvpCancelledEvent(this, eventId, rsvpId));
    }
}