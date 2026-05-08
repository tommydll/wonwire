package com.wonwire.wonwire.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyStatsDTO {
    private int year;
    private int month;
    private BigDecimal sent;
    private BigDecimal received;
}