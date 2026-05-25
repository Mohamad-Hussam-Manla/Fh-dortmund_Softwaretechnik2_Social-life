package de.fhdortmund.mystudyapp.identity.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.fhdortmund.mystudyapp.common.response.ApiResponse;
import de.fhdortmund.mystudyapp.identity.dto.PublicProfileDto;
import de.fhdortmund.mystudyapp.identity.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/public/users")
@RequiredArgsConstructor
public class PublicUserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<PublicProfileDto>> getPublicProfile(@PathVariable UUID userId) {
        PublicProfileDto profile = userService.getPublicProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile retrieved"));
    }
}