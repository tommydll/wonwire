package com.wonwire.wonwire.service;

import com.wonwire.wonwire.domain.Transaction;
import com.wonwire.wonwire.domain.Transaction.TransactionStatus;
import com.wonwire.wonwire.domain.User;
import com.wonwire.wonwire.domain.Wallet;
import com.wonwire.wonwire.dto.TransactionResponseDTO;
import com.wonwire.wonwire.dto.TransferRequestDTO;
import com.wonwire.wonwire.dto.TransferResponseDTO;
import com.wonwire.wonwire.exception.InsufficientBalanceException;
import com.wonwire.wonwire.exception.InvalidTransferException;
import com.wonwire.wonwire.exception.UserNotFoundException;
import com.wonwire.wonwire.repository.TransactionRepository;
import com.wonwire.wonwire.repository.UserRepository;
import com.wonwire.wonwire.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Initiates a transfer between two users.
     * Checks idempotency, validates the transfer, locks wallets,
     * debits the sender and credits the receiver atomically.
     */
    @Transactional
    public TransferResponseDTO transfer(User sender, TransferRequestDTO request) {

        // Idempotency check — return existing transaction if key already exists
        return transactionRepository.findByIdempotencyKey(request.getIdempotencyKey())
                .map(this::toTransferResponseDTO)
                .orElseGet(() -> executeTransfer(sender, request));
    }

    private TransferResponseDTO executeTransfer(User sender, TransferRequestDTO request) {

        // Cannot send money to yourself
        if (sender.getEmail().equals(request.getToEmail())) {
            throw new InvalidTransferException("Cannot transfer money to yourself");
        }

        // Find receiver
        User receiver = userRepository.findByEmail(request.getToEmail())
                .orElseThrow(() -> new UserNotFoundException(request.getToEmail()));

        // Find sender wallet and lock it to prevent concurrent modifications
        Wallet senderWallet = walletRepository.findWithLockByUser(sender)
                .orElseThrow(() -> new UserNotFoundException(sender.getEmail()));

        // Check sufficient balance
        if (senderWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException();
        }

        // Find receiver wallet and lock it to prevent concurrent modifications
        Wallet receiverWallet = walletRepository.findWithLockByUser(receiver)
                .orElseThrow(() -> new UserNotFoundException(receiver.getEmail()));

        // Debit sender
        senderWallet.setBalance(senderWallet.getBalance().subtract(request.getAmount()));
        walletRepository.save(senderWallet);

        // Credit receiver
        receiverWallet.setBalance(receiverWallet.getBalance().add(request.getAmount()));
        walletRepository.save(receiverWallet);

        // Save transaction
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(request.getAmount())
                .currency(senderWallet.getCurrency())
                .description(request.getDescription())
                .idempotencyKey(request.getIdempotencyKey())
                .status(TransactionStatus.SUCCESS)
                .build();

        transactionRepository.save(transaction);

        return toTransferResponseDTO(transaction);
    }

    /**
     * Retrieves the paginated transaction history for the authenticated user.
     * Returns all transactions where the user is either sender or receiver.
     */
    public Page<TransactionResponseDTO> getHistory(User user, Pageable pageable) {
        return transactionRepository
                .findBySenderOrReceiver(user, user, pageable)
                .map(this::toTransactionResponseDTO);
    }

    private TransferResponseDTO toTransferResponseDTO(Transaction transaction) {
        return TransferResponseDTO.builder()
                .transactionId(transaction.getId())
                .fromEmail(transaction.getSender().getEmail())
                .toEmail(transaction.getReceiver().getEmail())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .description(transaction.getDescription())
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    private TransactionResponseDTO toTransactionResponseDTO(Transaction transaction) {
        return TransactionResponseDTO.builder()
                .transactionId(transaction.getId())
                .fromEmail(transaction.getSender().getEmail())
                .toEmail(transaction.getReceiver().getEmail())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .description(transaction.getDescription())
                .status(transaction.getStatus())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}