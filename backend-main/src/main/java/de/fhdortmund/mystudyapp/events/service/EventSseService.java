package de.fhdortmund.mystudyapp.events.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

/**
 * PHASE 1.4: Server-Sent Events service for real-time event updates.
 * Manages SseEmitter subscriptions per event and broadcasts updates
 * when RSVP counts change.
 */
@Slf4j
@Service
public class EventSseService {

    /** Emitter timeout: 5 minutes (clients should reconnect) */
    private static final long EMITTER_TIMEOUT_MS = 300_000L;

    /** Map: eventId → list of active emitters */
    private final Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    /**
     * Subscribe a client to real-time updates for a specific event.
     * The client should reconnect before timeout expires.
     */
    public SseEmitter subscribe(UUID eventId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MS);

        List<SseEmitter> eventEmitters = emitters.computeIfAbsent(
                eventId, k -> new CopyOnWriteArrayList<>());
        eventEmitters.add(emitter);

        // Clean up on completion, timeout, or error
        emitter.onCompletion(() -> removeEmitter(eventId, emitter));
        emitter.onTimeout(() -> removeEmitter(eventId, emitter));
        emitter.onError(e -> removeEmitter(eventId, emitter));

        // Send initial connection confirmation
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of("eventId", eventId, "status", "subscribed")));
        } catch (IOException e) {
            log.warn("Failed to send SSE connection confirmation for event {}", eventId);
            removeEmitter(eventId, emitter);
        }

        log.debug("SSE subscription created for event {}. Active subscribers: {}",
                eventId, eventEmitters.size());

        return emitter;
    }

    /**
     * Broadcast an RSVP count update to all subscribers of an event.
     * Called by RsvpService and WaitlistService when counts change.
     */
    public void notifyRsvpUpdate(UUID eventId, int currentCount, int maxCapacity) {
        List<SseEmitter> eventEmitters = emitters.get(eventId);
        if (eventEmitters == null || eventEmitters.isEmpty()) {
            return; // No subscribers, skip
        }

        Map<String, Object> payload = Map.of(
                "eventId", eventId,
                "currentCount", currentCount,
                "maxCapacity", maxCapacity,
                "spotsRemaining", Math.max(0, maxCapacity - currentCount),
                "isFull", currentCount >= maxCapacity
        );

        for (SseEmitter emitter : eventEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("rsvp-update")
                        .data(payload));
            } catch (IOException e) {
                log.debug("SSE send failed for event {}, removing dead emitter", eventId);
                removeEmitter(eventId, emitter);
            }
        }

        log.debug("SSE rsvp-update sent to {} subscribers for event {}",
                eventEmitters.size(), eventId);
    }

    /**
     * Broadcast a waitlist promotion to all subscribers.
     * Shows updated waitlist position for remaining waitlisted users.
     */
    public void notifyWaitlistUpdate(UUID eventId, int waitlistCount) {
        List<SseEmitter> eventEmitters = emitters.get(eventId);
        if (eventEmitters == null || eventEmitters.isEmpty()) {
            return;
        }

        Map<String, Object> payload = Map.of(
                "eventId", eventId,
                "waitlistCount", waitlistCount,
                "type", "waitlist-update"
        );

        for (SseEmitter emitter : eventEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("waitlist-update")
                        .data(payload));
            } catch (IOException e) {
                removeEmitter(eventId, emitter);
            }
        }
    }

    /**
     * Broadcast that an event was cancelled.
     * Forces all open event detail pages to refresh.
     */
    public void notifyEventCancelled(UUID eventId) {
        List<SseEmitter> eventEmitters = emitters.get(eventId);
        if (eventEmitters == null || eventEmitters.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : eventEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("event-cancelled")
                        .data(Map.of("eventId", eventId)));
            } catch (IOException e) {
                removeEmitter(eventId, emitter);
            }
        }

        log.info("SSE event-cancelled broadcast to {} subscribers for event {}",
                eventEmitters.size(), eventId);
    }

    private void removeEmitter(UUID eventId, SseEmitter emitter) {
        List<SseEmitter> eventEmitters = emitters.get(eventId);
        if (eventEmitters != null) {
            eventEmitters.remove(emitter);
            if (eventEmitters.isEmpty()) {
                emitters.remove(eventId);
            }
        }
    }
}

