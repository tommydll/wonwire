package com.wonwire.wonwire.controller;

import com.wonwire.wonwire.domain.User;
import com.wonwire.wonwire.dto.WalletResponseDTO;
import com.wonwire.wonwire.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    /**
     * Returns the authenticated user's wallet balance.
     * GET /api/wallet/balance
     * Requires a valid JWT token in the Authorization header.
     */
    @GetMapping("/balance")
    public ResponseEntity<WalletResponseDTO> getBalance(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(walletService.getBalance(user));
    }
}