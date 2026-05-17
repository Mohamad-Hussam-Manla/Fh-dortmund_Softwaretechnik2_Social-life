package de.fhdortmund.mystudyapp.registration.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fhdortmund.mystudyapp.common.response.ApiResponse;
import de.fhdortmund.mystudyapp.common.response.PageResponse;
import de.fhdortmund.mystudyapp.registration.dto.RsvpDto;
import de.fhdortmund.mystudyapp.registration.model.RsvpStatus;
import de.fhdortmund.mystudyapp.registration.service.RsvpService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RsvpController {

    private final RsvpService rsvpService;

    /* -------------------- Participant Operations -------------------- */

    @PostMapping("/events/{eventId}/rsvps")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RsvpDto>> createRsvp(
            @PathVariable UUID eventId,
            @AuthenticationPrincipal User principal) {
        RsvpDto rsvp = rsvpService.createRsvp(eventId, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(rsvp, "Successfully registered for event"));
    }

    @GetMapping("/events/{eventId}/rsvps/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RsvpDto>> getMyRsvpForEvent(
            @PathVariable UUID eventId,
            @AuthenticationPrincipal User principal) {
        RsvpDto rsvp = rsvpService.getMyRsvpForEvent(eventId, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(rsvp, "Registration retrieved"));
    }

    @GetMapping("/rsvps/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<RsvpDto>>> getMyRsvps(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal User principal) {
        PageResponse<RsvpDto> rsvps = rsvpService.getMyRsvps(principal.getUsername(), pageable);
        return ResponseEntity.ok(ApiResponse.success(rsvps, "My registrations retrieved"));
    }

    @PatchMapping("/rsvps/{rsvpId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RsvpDto>> cancelRsvp(
            @PathVariable UUID rsvpId,
            @AuthenticationPrincipal User principal) {
        RsvpDto rsvp = rsvpService.cancelRsvp(rsvpId, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(rsvp, "Registration cancelled successfully"));
    }

    /* -------------------- Waitlist Position -------------------- */

    @GetMapping("/rsvps/{rsvpId}/position")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Integer>> getWaitlistPosition(
            @PathVariable UUID rsvpId,
            @AuthenticationPrincipal User principal) {
        int position = rsvpService.getWaitlistPosition(rsvpId, principal.getUsername());
        String message = position > 0
                ? "You are number " + position + " on the waitlist"
                : "You are not on the waitlist";
        return ResponseEntity.ok(ApiResponse.success(position, message));
    }

    /* -------------------- Host Operations -------------------- */

    @GetMapping("/events/{eventId}/rsvps")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<RsvpDto>>> getEventRsvps(
            @PathVariable UUID eventId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal User principal) {
        PageResponse<RsvpDto> rsvps = rsvpService.getEventRsvps(eventId, principal.getUsername(), pageable);
        return ResponseEntity.ok(ApiResponse.success(rsvps, "Event registrations retrieved"));
    }

    @GetMapping("/events/{eventId}/rsvps/status/{status}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<RsvpDto>>> getEventRsvpsByStatus(
            @PathVariable UUID eventId,
            @PathVariable RsvpStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal User principal) {
        PageResponse<RsvpDto> rsvps = rsvpService.getEventRsvpsByStatus(eventId, status, principal.getUsername(), pageable);
        return ResponseEntity.ok(ApiResponse.success(rsvps, "Event registrations filtered"));
    }

    @PatchMapping("/events/{eventId}/rsvps/{rsvpId}/attended")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RsvpDto>> markAttended(
            @PathVariable UUID eventId,
            @PathVariable UUID rsvpId,
            @AuthenticationPrincipal User principal) {
        RsvpDto rsvp = rsvpService.markAttended(eventId, rsvpId, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(rsvp, "Attendance marked successfully"));
    }
}