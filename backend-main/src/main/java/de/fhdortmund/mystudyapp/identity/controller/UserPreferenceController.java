package de.fhdortmund.mystudyapp.identity.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fhdortmund.mystudyapp.common.response.ApiResponse;
import de.fhdortmund.mystudyapp.identity.dto.UserPreferencesDto;
import de.fhdortmund.mystudyapp.identity.service.UserPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * PHASE 3: User Preferences / Settings Endpoints
 * 
 * GET  /api/auth/me/preferences  → Retrieve current preferences
 * PUT  /api/auth/me/preferences  → Update preferences
 */
@RestController
@RequestMapping("/api/auth/me/preferences")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserPreferencesDto>> getPreferences(
            @AuthenticationPrincipal User principal) {
        UserPreferencesDto prefs = userPreferenceService.getPreferences(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(prefs, "Preferences retrieved"));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserPreferencesDto>> updatePreferences(
            @AuthenticationPrincipal User principal,
            @Valid @RequestBody UserPreferencesDto request) {
        UserPreferencesDto updated = userPreferenceService.updatePreferences(principal.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Preferences updated successfully"));
    }
}