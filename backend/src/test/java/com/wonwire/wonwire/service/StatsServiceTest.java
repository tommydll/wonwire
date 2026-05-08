package com.wonwire.wonwire.service;

import com.wonwire.wonwire.domain.User;
import com.wonwire.wonwire.dto.StatsResponseDTO;
import com.wonwire.wonwire.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private StatsService statsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("alice@wonwire.com")
                .firstName("Alice")
                .lastName("Smith")
                .password("encoded")
                .build();
    }

    // -------------------------------------------------------------------------
    // getStats()
    // -------------------------------------------------------------------------

    @Test
    void getStats_ShouldReturnSixMonths_WhenNoTransactions() {
        // Given
        when(transactionRepository.findMonthlySentByUser(eq(user.getId()), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(transactionRepository.findMonthlyReceivedByUser(eq(user.getId()), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // When
        StatsResponseDTO response = statsService.getStats(user);

        // Then — always 6 months even with no data
        assertThat(response.getMonthly()).hasSize(6);
        assertThat(response.getTotalSent()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getTotalReceived()).isEqualByComparingTo(BigDecimal.ZERO);
        response.getMonthly().forEach(m -> {
            assertThat(m.getSent()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(m.getReceived()).isEqualByComparingTo(BigDecimal.ZERO);
        });
    }

    @Test
    void getStats_ShouldCalculateTotalsCorrectly_WhenTransactionsExist() {
        // Given — simulate current month data
        LocalDateTime now = LocalDateTime.now();
        Object[] sentRow = {now.getYear(), now.getMonthValue(), new BigDecimal("80000")};
        Object[] receivedRow = {now.getYear(), now.getMonthValue(), new BigDecimal("130000")};

        when(transactionRepository.findMonthlySentByUser(eq(user.getId()), any(LocalDateTime.class)))
                .thenReturn(List.<Object[]>of(sentRow));
        when(transactionRepository.findMonthlyReceivedByUser(eq(user.getId()), any(LocalDateTime.class)))
                .thenReturn(List.<Object[]>of(receivedRow));

        // When
        StatsResponseDTO response = statsService.getStats(user);

        // Then
        assertThat(response.getMonthly()).hasSize(6);
        assertThat(response.getTotalSent()).isEqualByComparingTo(new BigDecimal("80000"));
        assertThat(response.getTotalReceived()).isEqualByComparingTo(new BigDecimal("130000"));
    }

    @Test
    void getStats_ShouldReturnMonthsInChronologicalOrder() {
        // Given
        when(transactionRepository.findMonthlySentByUser(eq(user.getId()), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(transactionRepository.findMonthlyReceivedByUser(eq(user.getId()), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // When
        StatsResponseDTO response = statsService.getStats(user);

        // Then — months must be in chronological order (oldest first)
        for (int i = 0; i < response.getMonthly().size() - 1; i++) {
            var current = response.getMonthly().get(i);
            var next = response.getMonthly().get(i + 1);
            boolean isChronological = current.getYear() < next.getYear() ||
                    (current.getYear() == next.getYear() && current.getMonth() < next.getMonth());
            assertThat(isChronological).isTrue();
        }
    }
}