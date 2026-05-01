package com.wonwire.wonwire.controller;

import com.wonwire.wonwire.domain.User;
import com.wonwire.wonwire.dto.TransactionResponseDTO;
import com.wonwire.wonwire.dto.TransferRequestDTO;
import com.wonwire.wonwire.dto.TransferResponseDTO;
import com.wonwire.wonwire.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    /**
     * Initiates a transfer between two users.
     * POST /api/transfers
     * Requires a valid JWT token in the Authorization header.
     */
    @PostMapping
    public ResponseEntity<TransferResponseDTO> transfer(
            @AuthenticationPrincipal User sender,
            @Valid @RequestBody TransferRequestDTO request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transferService.transfer(sender, request));
    }

    /**
     * Retrieves the paginated transaction history for the authenticated user.
     * GET /api/transfers?page=0&size=10&sort=createdAt,desc
     * Requires a valid JWT token in the Authorization header.
     */
    @GetMapping
    public ResponseEntity<Page<TransactionResponseDTO>> getHistory(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(transferService.getHistory(user, pageable));
    }
}