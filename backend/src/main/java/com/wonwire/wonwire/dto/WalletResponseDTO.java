package com.wonwire.wonwire.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Response DTO returned when a user requests their wallet balance.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletResponseDTO {
    private BigDecimal balance;
    private String email;
    private String fullName;
}