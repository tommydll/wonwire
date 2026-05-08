package com.wonwire.wonwire.controller;

import com.wonwire.wonwire.domain.User;
import com.wonwire.wonwire.dto.ChangePasswordRequestDTO;
import com.wonwire.wonwire.dto.MessageResponseDTO;
import com.wonwire.wonwire.dto.UpdateProfileRequestDTO;
import com.wonwire.wonwire.dto.UserProfileResponseDTO;
import com.wonwire.wonwire.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Returns the authenticated user's profile.
     * GET /api/user/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponseDTO> getProfile(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getProfile(user));
    }

    /**
     * Updates the authenticated user's profile (firstName, lastName, email).
     * PUT /api/user/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponseDTO> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProfileRequestDTO request) {
        return ResponseEntity.ok(userService.updateProfile(user, request));
    }

    /**
     * Changes the authenticated user's password.
     * POST /api/user/change-password
     */
    @PostMapping("/change-password")
    public ResponseEntity<MessageResponseDTO> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequestDTO request) {
        return ResponseEntity.ok(userService.changePassword(user, request));
    }
}