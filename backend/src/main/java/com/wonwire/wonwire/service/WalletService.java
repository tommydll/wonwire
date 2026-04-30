package com.wonwire.wonwire.service;

import com.wonwire.wonwire.domain.User;
import com.wonwire.wonwire.domain.Wallet;
import com.wonwire.wonwire.dto.WalletResponseDTO;
import com.wonwire.wonwire.exception.UserNotFoundException;
import com.wonwire.wonwire.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    /**
     * Retrieves the wallet balance for the authenticated user.
     * The user is extracted from the Spring Security context by the controller.
     */
    public WalletResponseDTO getBalance(User user) {
        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new UserNotFoundException(user.getEmail()));

        return WalletResponseDTO.builder()
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }
}