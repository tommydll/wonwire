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
 * Response DTO for a single transaction in the paginated history.
 * Used when retrieving the authenticated user's transaction history.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponseDTO {
    private Long transactionId;
    private String fromEmail;
    private String toEmail;
    private BigDecimal amount;
    private Currency currency;
    private String description;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}