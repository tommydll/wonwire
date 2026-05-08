package com.wonwire.wonwire.controller;

import com.wonwire.wonwire.domain.User;
import com.wonwire.wonwire.dto.StatsResponseDTO;
import com.wonwire.wonwire.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /**
     * Returns monthly spending statistics for the authenticated user.
     * Covers the last 6 months, transfers only (deposits excluded).
     * GET /api/stats
     * Requires a valid JWT token in the Authorization header.
     */
    @GetMapping
    public ResponseEntity<StatsResponseDTO> getStats(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(statsService.getStats(user));
    }
}