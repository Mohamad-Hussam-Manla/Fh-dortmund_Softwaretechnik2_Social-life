package de.fhdortmund.mystudyapp.events.controller;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.fhdortmund.mystudyapp.common.exception.ResourceNotFoundException;
import de.fhdortmund.mystudyapp.common.response.ApiResponse;
import de.fhdortmund.mystudyapp.common.response.PageResponse;
import de.fhdortmund.mystudyapp.events.dto.EventDto;
import de.fhdortmund.mystudyapp.events.model.EventStatus;
import de.fhdortmund.mystudyapp.events.service.EventService;
import lombok.RequiredArgsConstructor;

/**
 * PHASE 1.2: Public Event Endpoints
 * 
 * Serves PUBLISHED events to unauthenticated users (landing page, SEO, sharing).
 * No Bearer token required. Personal fields (myRsvpStatus, isHost) are stripped
 * by passing null as the current user.
 * 
 * Endpoints:
 *   GET /api/public/events          → Filtered feed of published events
 *   GET /api/public/events/{id}     → Single published event detail
 *   GET /api/public/events/featured → Curated featured events (optional)
 *   GET /api/public/events/slug/{slug} → Get by slug (PHASE 2)
 */
@RestController
@RequestMapping("/api/public/events")
@RequiredArgsConstructor
public class PublicEventController {

    private final EventService eventService;

    /**
     * Public event feed — no authentication required.
     * Returns only PUBLISHED events. myRsvpStatus and isHost are always null/false.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<EventDto>>> getPublicEvents(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Instant dateFrom,
            @RequestParam(required = false) Instant dateTo,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "startTime") Pageable pageable) {

        // Pass null as email → no auth user → myRsvpStatus = null, isHost = false
        PageResponse<EventDto> events = eventService.getPublishedEvents(
                categoryId, dateFrom, dateTo, location, q, pageable, null);

        return ResponseEntity.ok(ApiResponse.success(events, "Events retrieved"));
    }

    /**
     * Public single event detail — no authentication required.
     * Only returns the event if its status is PUBLISHED.
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<ApiResponse<EventDto>> getPublicEvent(@PathVariable UUID eventId) {
        // Pass null as email → no auth context
        EventDto event = eventService.getEvent(eventId, null);

        // Security: never expose non-published events publicly
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Event not found or not published");
        }

        return ResponseEntity.ok(ApiResponse.success(event, "Event retrieved"));
    }

    /**
     * PHASE 2: Get event by slug — public, no auth required.
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<EventDto>> getPublicEventBySlug(@PathVariable String slug) {
        EventDto event = eventService.getEventBySlug(slug, null);

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Event not found or not published");
        }

        return ResponseEntity.ok(ApiResponse.success(event, "Event retrieved"));
    }

    /**
     * Featured events for the landing page hero section.
     * Returns the next 6 upcoming PUBLISHED events, ordered by start time.
     */
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<PageResponse<EventDto>>> getFeaturedEvents(
            @PageableDefault(size = 6, sort = "startTime") Pageable pageable) {

        // Featured = upcoming published events, no additional filters
        PageResponse<EventDto> events = eventService.getPublishedEvents(
                null, Instant.now(), null, null, null, pageable, null);

        return ResponseEntity.ok(ApiResponse.success(events, "Featured events retrieved"));
    }
}