package com.wonwire.wonwire.service;

import com.wonwire.wonwire.domain.User;
import com.wonwire.wonwire.dto.MonthlyStatsDTO;
import com.wonwire.wonwire.dto.StatsResponseDTO;
import com.wonwire.wonwire.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final TransactionRepository transactionRepository;

    /**
     * Returns monthly sent and received statistics for the authenticated user over the last 6 months.
     * Deposits are excluded, only transfers are counted.
     */
    public StatsResponseDTO getStats(User user) {
        LocalDateTime since = LocalDateTime.now().minusMonths(6);

        List<Object[]> sentRaw = transactionRepository.findMonthlySentByUser(user.getId(), since);
        List<Object[]> receivedRaw = transactionRepository.findMonthlyReceivedByUser(user.getId(), since);

        // Map year-month -> amount for sent
        Map<String, BigDecimal> sentMap = new HashMap<>();
        for (Object[] row : sentRaw) {
            String key = row[0] + "-" + row[1];
            sentMap.put(key, (BigDecimal) row[2]);
        }

        // Map year-month -> amount for received
        Map<String, BigDecimal> receivedMap = new HashMap<>();
        for (Object[] row : receivedRaw) {
            String key = row[0] + "-" + row[1];
            receivedMap.put(key, (BigDecimal) row[2]);
        }

        // Build the last 6 months in order
        List<MonthlyStatsDTO> monthly = new ArrayList<>();
        BigDecimal totalSent = BigDecimal.ZERO;
        BigDecimal totalReceived = BigDecimal.ZERO;

        for (int i = 5; i >= 0; i--) {
            LocalDateTime month = LocalDateTime.now().minusMonths(i);
            int year = month.getYear();
            int monthNum = month.getMonthValue();
            String key = year + "-" + monthNum;

            BigDecimal sent = sentMap.getOrDefault(key, BigDecimal.ZERO);
            BigDecimal received = receivedMap.getOrDefault(key, BigDecimal.ZERO);

            totalSent = totalSent.add(sent);
            totalReceived = totalReceived.add(received);

            monthly.add(MonthlyStatsDTO.builder()
                    .year(year)
                    .month(monthNum)
                    .sent(sent)
                    .received(received)
                    .build());
        }

        return StatsResponseDTO.builder()
                .monthly(monthly)
                .totalSent(totalSent)
                .totalReceived(totalReceived)
                .build();
    }
}