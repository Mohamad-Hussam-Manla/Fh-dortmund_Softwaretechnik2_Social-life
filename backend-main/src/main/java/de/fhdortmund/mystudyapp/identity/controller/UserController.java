package de.fhdortmund.mystudyapp.identity.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.fhdortmund.mystudyapp.common.response.ApiResponse;
import de.fhdortmund.mystudyapp.identity.dto.AuthResponse;
import de.fhdortmund.mystudyapp.identity.dto.ChangePasswordRequest;
import de.fhdortmund.mystudyapp.identity.dto.ForgotPasswordRequest;
import de.fhdortmund.mystudyapp.identity.dto.LoginRequest;
import de.fhdortmund.mystudyapp.identity.dto.RegisterRequest;
import de.fhdortmund.mystudyapp.identity.dto.ResetPasswordRequest;
import de.fhdortmund.mystudyapp.identity.dto.TrustQualificationStatus;
import de.fhdortmund.mystudyapp.identity.dto.UpdateProfileRequest;
import de.fhdortmund.mystudyapp.identity.dto.UserDto;
import de.fhdortmund.mystudyapp.identity.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /* -------------------- Authentication -------------------- */

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse auth = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(auth, "Registration successful"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse auth = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success(auth, "Login successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestHeader("X-Refresh-Token") String refreshToken) {
        AuthResponse auth = userService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(auth, "Token refreshed"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        userService.logout(token);
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        userService.verifyAccount(token);
        return ResponseEntity.ok(ApiResponse.success(null, "Account verified. You can now log in."));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @Valid @RequestBody ForgotPasswordRequest request) {
        userService.resendVerificationEmail(request.getUniversityEmail());
        return ResponseEntity.ok(ApiResponse.success(null,
                "If that email is registered and not verified, a new verification link has been sent."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        userService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null,
                "If that email is registered, a reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(null,
                "Password updated successfully. You can now log in."));
    }

    /* -------------------- Profile (single endpoint) -------------------- */

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserDto>> getMe(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        UserDto user = userService.getCurrentUser(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved"));
    }

    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @Valid @ModelAttribute UpdateProfileRequest request) {

        UserDto updated = userService.updateProfile(principal.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Profile updated successfully"));
    }

    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(principal.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        userService.deleteAccount(principal.getUsername(), token);
        return ResponseEntity.ok(ApiResponse.success(null, "Account deleted successfully"));
    }

    /* -------------------- Trust Qualification -------------------- */

    @GetMapping("/me/trust-status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TrustQualificationStatus>> getMyTrustStatus(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        TrustQualificationStatus status = userService.getTrustQualificationStatusForCurrentUser(
                principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success(status, "Trust qualification status retrieved"));
    }
}