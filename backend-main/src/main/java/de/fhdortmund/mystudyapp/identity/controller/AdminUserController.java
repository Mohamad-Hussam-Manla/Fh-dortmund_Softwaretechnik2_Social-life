package de.fhdortmund.mystudyapp.identity.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.fhdortmund.mystudyapp.common.response.ApiResponse;
import de.fhdortmund.mystudyapp.common.response.PageResponse;
import de.fhdortmund.mystudyapp.identity.dto.UserDto;
import de.fhdortmund.mystudyapp.identity.model.TrustLevel;
import de.fhdortmund.mystudyapp.identity.service.TrustLevelService;
import de.fhdortmund.mystudyapp.identity.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final TrustLevelService trustLevelService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserDto>>> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TrustLevel trustLevel,
            Pageable pageable) {

        Page<UserDto> page;
        if (trustLevel != null) {
            page = userService.searchUsersByTrustLevel(search, trustLevel, pageable);
        } else {
            page = userService.searchUsers(search, pageable);
        }

        PageResponse<UserDto> response = PageResponse.<UserDto>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Users retrieved"));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDto>> getUser(@PathVariable UUID userId) {
        UserDto user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved"));
    }

    @PatchMapping("/{userId}/trust-level")
    public ResponseEntity<ApiResponse<Void>> updateTrustLevel(
            @PathVariable UUID userId,
            @RequestParam TrustLevel trustLevel) {

        trustLevelService.updateTrustLevel(userId, trustLevel);
        return ResponseEntity.ok(ApiResponse.success(null,
            "Trust level updated to " + trustLevel));
    }

    @PostMapping("/{userId}/flag")
    public ResponseEntity<ApiResponse<Void>> flagUser(@PathVariable UUID userId) {
        trustLevelService.flagUser(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "User has been flagged"));
    }

    @PostMapping("/{userId}/promote")
    public ResponseEntity<ApiResponse<Void>> promoteUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "false") boolean force) {
        if (force) {
            trustLevelService.forcePromoteToTrustedHost(userId);
        } else {
            trustLevelService.promoteToTrustedHost(userId);
        }
        return ResponseEntity.ok(ApiResponse.success(null, "User promoted to trusted host"));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID userId) {
        userService.deleteUserByAdmin(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }
}