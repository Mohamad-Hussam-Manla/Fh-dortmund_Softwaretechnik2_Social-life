package de.fhdortmund.mystudyapp.registration.observer;

import java.util.UUID;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class RsvpCancelledEvent extends ApplicationEvent {

    private final UUID eventId;
    private final UUID rsvpId;

    public RsvpCancelledEvent(Object source, UUID eventId, UUID rsvpId) {
        super(source);
        this.eventId = eventId;
        this.rsvpId = rsvpId;
    }
}