package com.wonwire.wonwire.controller;

import com.wonwire.wonwire.domain.User;
import com.wonwire.wonwire.dto.DepositRequestDTO;
import com.wonwire.wonwire.dto.DepositResponseDTO;
import com.wonwire.wonwire.dto.WalletResponseDTO;
import com.wonwire.wonwire.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    /**
     * Deposits funds into the authenticated user's wallet.
     * Simulates a bank transfer from the selected payment method.
     * POST /api/wallet/deposit
     * Requires a valid JWT token in the Authorization header.
     */
    @PostMapping("/deposit")
    public ResponseEntity<DepositResponseDTO> deposit(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody DepositRequestDTO request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(walletService.deposit(user, request));
    }
}