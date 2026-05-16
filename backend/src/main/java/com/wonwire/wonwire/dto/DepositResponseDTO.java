package com.wonwire.wonwire.dto;

import com.wonwire.wonwire.domain.enums.Currency;
import com.wonwire.wonwire.domain.enums.TransactionStatus;
import com.wonwire.wonwire.domain.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO returned after a successful deposit.
 * Contains the updated wallet balance and the transaction details.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DepositResponseDTO {
    private Long transactionId;
    private String email;
    private BigDecimal amount;
    private BigDecimal newBalance;
    private Currency currency;
    private TransactionStatus status;
    private TransactionType type;
    private String paymentMethod;
    private LocalDateTime createdAt;
}