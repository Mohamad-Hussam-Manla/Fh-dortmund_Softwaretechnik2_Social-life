package de.fhdortmund.mystudyapp.events.controller;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import de.fhdortmund.mystudyapp.events.service.EventSseService;
import lombok.RequiredArgsConstructor;

/**
 * PHASE 1.4: Server-Sent Events endpoint for real-time event updates.
 *
 * Clients subscribe via:
 *   const es = new EventSource('/api/events/stream/{eventId}');
 *   es.addEventListener('rsvp-update', (e) => { ... });
 *   es.addEventListener('waitlist-update', (e) => { ... });
 *   es.addEventListener('event-cancelled', (e) => { ... });
 *
 * The connection times out after 5 minutes — clients should reconnect.
 */
@RestController
@RequestMapping("/api/events/stream")
@RequiredArgsConstructor
public class EventSseController {

    private final EventSseService sseService;

    /**
     * Subscribe to real-time updates for a specific event.
     * Returns text/event-stream content type.
     */
    @GetMapping(value = "/{eventId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToEvent(@PathVariable UUID eventId) {
        return sseService.subscribe(eventId);
    }
}

