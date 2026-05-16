package com.wonwire.wonwire.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

/**
 * Request DTO for depositing funds into a wallet.
 * The user specifies the amount and the simulated payment method (Kakao Pay, Toss, Shinhan).
 */
@Data
public class DepositRequestDTO {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000", message = "Minimum deposit amount is ₩1,000")
    private BigDecimal amount;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
}