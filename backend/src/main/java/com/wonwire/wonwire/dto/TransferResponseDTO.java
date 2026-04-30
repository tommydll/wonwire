package com.wonwire.wonwire.dto;

import com.wonwire.wonwire.domain.Transaction.TransactionStatus;
import com.wonwire.wonwire.domain.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO returned immediately after a transfer is initiated.
 * Contains the transaction details and its current status.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponseDTO {
    private Long transactionId;
    private String fromEmail;
    private String toEmail;
    private BigDecimal amount;
    private Currency currency;
    private String description;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}