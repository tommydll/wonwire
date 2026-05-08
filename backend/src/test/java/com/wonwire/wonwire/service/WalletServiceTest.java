package com.wonwire.wonwire.service;

import com.wonwire.wonwire.domain.Transaction;
import com.wonwire.wonwire.domain.User;
import com.wonwire.wonwire.domain.Wallet;
import com.wonwire.wonwire.domain.enums.Currency;
import com.wonwire.wonwire.domain.enums.TransactionStatus;
import com.wonwire.wonwire.domain.enums.TransactionType;
import com.wonwire.wonwire.dto.DepositRequestDTO;
import com.wonwire.wonwire.dto.DepositResponseDTO;
import com.wonwire.wonwire.dto.WalletResponseDTO;
import com.wonwire.wonwire.exception.UserNotFoundException;
import com.wonwire.wonwire.repository.TransactionRepository;
import com.wonwire.wonwire.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private WalletService walletService;

    private User user;
    private Wallet wallet;
    private DepositRequestDTO depositRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("alice@wonwire.com")
                .password("encodedPassword")
                .firstName("Alice")
                .lastName("Smith")
                .build();

        wallet = Wallet.builder()
                .user(user)
                .balance(new BigDecimal("100000"))
                .currency(Currency.KRW)
                .build();

        depositRequest = new DepositRequestDTO();
        depositRequest.setAmount(new BigDecimal("50000"));
        depositRequest.setPaymentMethod("Kakao Pay");
    }

    // -------------------------------------------------------------------------
    // getBalance()
    // -------------------------------------------------------------------------

    @Test
    void getBalance_ShouldReturnWalletInfo_WhenUserHasWallet() {
        // Given
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));

        // When
        WalletResponseDTO response = walletService.getBalance(user);

        // Then
        assertThat(response.getBalance()).isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(response.getCurrency()).isEqualTo(Currency.KRW);
        assertThat(response.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void getBalance_ShouldThrowUserNotFoundException_WhenWalletNotFound() {
        // Given
        when(walletRepository.findByUser(user)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> walletService.getBalance(user))
                .isInstanceOf(UserNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // deposit()
    // -------------------------------------------------------------------------

    @Test
    void deposit_ShouldCreditWalletAndSaveTransaction_WhenRequestIsValid() {
        // Given
        when(walletRepository.findWithLockByUser(user)).thenReturn(Optional.of(wallet));
        Transaction savedTransaction = Transaction.builder()
                .sender(user)
                .receiver(user)
                .amount(depositRequest.getAmount())
                .currency(Currency.KRW)
                .description("Deposit via Kakao Pay")
                .idempotencyKey("some-uuid")
                .status(TransactionStatus.SUCCESS)
                .type(TransactionType.DEPOSIT)
                .build();
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        // When
        DepositResponseDTO response = walletService.deposit(user, depositRequest);

        // Then
        assertThat(wallet.getBalance()).isEqualByComparingTo(new BigDecimal("150000"));
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("50000"));
        assertThat(response.getNewBalance()).isEqualByComparingTo(new BigDecimal("150000"));
        assertThat(response.getPaymentMethod()).isEqualTo("Kakao Pay");
        assertThat(response.getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(response.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        verify(walletRepository, times(1)).save(any(Wallet.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void deposit_ShouldThrowUserNotFoundException_WhenWalletNotFound() {
        // Given
        when(walletRepository.findWithLockByUser(user)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> walletService.deposit(user, depositRequest))
                .isInstanceOf(UserNotFoundException.class);

        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }
}