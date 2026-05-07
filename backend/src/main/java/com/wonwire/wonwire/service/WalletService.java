package com.wonwire.wonwire.service;

import com.wonwire.wonwire.domain.Transaction;
import com.wonwire.wonwire.domain.User;
import com.wonwire.wonwire.domain.Wallet;
import com.wonwire.wonwire.domain.enums.TransactionStatus;
import com.wonwire.wonwire.domain.enums.TransactionType;
import com.wonwire.wonwire.dto.DepositRequestDTO;
import com.wonwire.wonwire.dto.DepositResponseDTO;
import com.wonwire.wonwire.dto.WalletResponseDTO;
import com.wonwire.wonwire.exception.UserNotFoundException;
import com.wonwire.wonwire.repository.TransactionRepository;
import com.wonwire.wonwire.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

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

    /**
     * Deposits funds into the authenticated user's wallet.
     * Simulates a bank transfer from the selected payment method (Kakao Pay, Toss, Shinhan).
     * Records a DEPOSIT transaction for history tracking.
     */
    @Transactional
    public DepositResponseDTO deposit(User user, DepositRequestDTO request) {
        Wallet wallet = walletRepository.findWithLockByUser(user)
                .orElseThrow(() -> new UserNotFoundException(user.getEmail()));

        // Credit the wallet
        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        walletRepository.save(wallet);

        // Record the deposit as a transaction for history tracking
        Transaction transaction = Transaction.builder()
                .sender(user)
                .receiver(user)
                .amount(request.getAmount())
                .currency(wallet.getCurrency())
                .description("Deposit via " + request.getPaymentMethod())
                .idempotencyKey(java.util.UUID.randomUUID().toString())
                .status(TransactionStatus.SUCCESS)
                .type(TransactionType.DEPOSIT)
                .build();

        transactionRepository.save(transaction);

        return DepositResponseDTO.builder()
                .transactionId(transaction.getId())
                .email(user.getEmail())
                .amount(request.getAmount())
                .newBalance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .status(TransactionStatus.SUCCESS)
                .type(TransactionType.DEPOSIT)
                .paymentMethod(request.getPaymentMethod())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}