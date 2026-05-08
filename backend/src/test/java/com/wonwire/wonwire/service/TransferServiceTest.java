package com.wonwire.wonwire.service;

import com.wonwire.wonwire.domain.Transaction;
import com.wonwire.wonwire.domain.enums.TransactionStatus;
import com.wonwire.wonwire.domain.User;
import com.wonwire.wonwire.domain.Wallet;
import com.wonwire.wonwire.domain.enums.Currency;
import com.wonwire.wonwire.dto.TransactionResponseDTO;
import com.wonwire.wonwire.dto.TransferRequestDTO;
import com.wonwire.wonwire.dto.TransferResponseDTO;
import com.wonwire.wonwire.exception.InsufficientBalanceException;
import com.wonwire.wonwire.exception.InvalidTransferException;
import com.wonwire.wonwire.exception.UserNotFoundException;
import com.wonwire.wonwire.repository.TransactionRepository;
import com.wonwire.wonwire.repository.UserRepository;
import com.wonwire.wonwire.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransferService transferService;

    private User sender;
    private User receiver;
    private Wallet senderWallet;
    private Wallet receiverWallet;
    private TransferRequestDTO request;
    private Transaction existingTransaction;

    @BeforeEach
    void setUp() {
        sender = User.builder()
                .email("sender@wonwire.com")
                .password("encodedPassword")
                .firstName("Alice")
                .lastName("Smith")
                .build();

        receiver = User.builder()
                .email("receiver@wonwire.com")
                .password("encodedPassword")
                .firstName("Bob")
                .lastName("Martin")
                .build();

        senderWallet = Wallet.builder()
                .user(sender)
                .balance(new BigDecimal("100000"))
                .currency(Currency.KRW)
                .build();

        receiverWallet = Wallet.builder()
                .user(receiver)
                .balance(new BigDecimal("50000"))
                .currency(Currency.KRW)
                .build();

        request = new TransferRequestDTO();
        request.setToEmail("receiver@wonwire.com");
        request.setAmount(new BigDecimal("30000"));
        request.setDescription("Lunch");
        request.setIdempotencyKey(UUID.randomUUID().toString());

        existingTransaction = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(new BigDecimal("30000"))
                .currency(Currency.KRW)
                .description("Lunch")
                .idempotencyKey(request.getIdempotencyKey())
                .status(TransactionStatus.SUCCESS)
                .build();
    }

    // -------------------------------------------------------------------------
    // transfer()
    // -------------------------------------------------------------------------

    @Test
    void transfer_ShouldDebitSenderAndCreditReceiver_WhenTransferIsValid() {
        // Given
        when(transactionRepository.findByIdempotencyKey(request.getIdempotencyKey())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getToEmail())).thenReturn(Optional.of(receiver));
        when(walletRepository.findWithLockByUser(sender)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findWithLockByUser(receiver)).thenReturn(Optional.of(receiverWallet));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(existingTransaction);

        // When
        TransferResponseDTO response = transferService.transfer(sender, request);

        // Then
        assertThat(senderWallet.getBalance()).isEqualByComparingTo(new BigDecimal("70000"));
        assertThat(receiverWallet.getBalance()).isEqualByComparingTo(new BigDecimal("80000"));
        assertThat(response.getFromEmail()).isEqualTo(sender.getEmail());
        assertThat(response.getToEmail()).isEqualTo(receiver.getEmail());
        assertThat(response.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void transfer_ShouldReturnExistingTransaction_WhenIdempotencyKeyAlreadyExists() {
        // Given — same key sent twice
        when(transactionRepository.findByIdempotencyKey(request.getIdempotencyKey()))
                .thenReturn(Optional.of(existingTransaction));

        // When
        TransferResponseDTO response = transferService.transfer(sender, request);

        // Then — no wallets touched, no new transaction saved
        assertThat(response.getFromEmail()).isEqualTo(sender.getEmail());
        assertThat(response.getToEmail()).isEqualTo(receiver.getEmail());
        verify(walletRepository, never()).findWithLockByUser(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transfer_ShouldThrowInvalidTransferException_WhenSenderEqualsReceiver() {
        // Given
        request.setToEmail(sender.getEmail());
        when(transactionRepository.findByIdempotencyKey(request.getIdempotencyKey())).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> transferService.transfer(sender, request))
                .isInstanceOf(InvalidTransferException.class)
                .hasMessageContaining("Cannot transfer money to yourself");

        verify(walletRepository, never()).findWithLockByUser(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transfer_ShouldThrowUserNotFoundException_WhenReceiverDoesNotExist() {
        // Given
        when(transactionRepository.findByIdempotencyKey(request.getIdempotencyKey())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getToEmail())).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> transferService.transfer(sender, request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(request.getToEmail());

        verify(walletRepository, never()).findWithLockByUser(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transfer_ShouldThrowInsufficientBalanceException_WhenBalanceIsTooLow() {
        // Given — sender only has 10000 but tries to send 30000
        senderWallet.setBalance(new BigDecimal("10000"));
        when(transactionRepository.findByIdempotencyKey(request.getIdempotencyKey())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getToEmail())).thenReturn(Optional.of(receiver));
        when(walletRepository.findWithLockByUser(sender)).thenReturn(Optional.of(senderWallet));

        // When / Then
        assertThatThrownBy(() -> transferService.transfer(sender, request))
                .isInstanceOf(InsufficientBalanceException.class);

        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // getHistory()
    // -------------------------------------------------------------------------

    @Test
    void getHistory_ShouldReturnPaginatedTransactions_ForAuthenticatedUser() {
        // Given
        Pageable pageable = PageRequest.of(0, 5);
        Page<Transaction> page = new PageImpl<>(List.of(existingTransaction), pageable, 1);
        when(transactionRepository.findBySenderOrReceiver(sender, sender, pageable)).thenReturn(page);

        // When
        Page<TransactionResponseDTO> result = transferService.getHistory(sender, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getFromEmail()).isEqualTo(sender.getEmail());
        assertThat(result.getContent().get(0).getToEmail()).isEqualTo(receiver.getEmail());
    }
}