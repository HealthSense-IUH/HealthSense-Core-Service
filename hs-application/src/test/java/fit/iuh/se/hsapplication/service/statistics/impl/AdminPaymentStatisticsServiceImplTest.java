package fit.iuh.se.hsapplication.service.statistics.impl;

import fit.iuh.se.hsbilling.repository.statistics.CreditPaymentStatisticsAmountBucket;
import fit.iuh.se.hsbilling.repository.statistics.CreditPaymentStatisticsCategoryCount;
import fit.iuh.se.hsbilling.repository.statistics.CreditPaymentStatisticsCountBucket;
import fit.iuh.se.hsbilling.repository.statistics.CreditPaymentStatisticsQueryRepository;
import fit.iuh.se.hsbilling.repository.statistics.CreditPaymentStatisticsSummary;
import fit.iuh.se.hschat.repository.statistics.DirectPaymentStatisticsAmountBucket;
import fit.iuh.se.hschat.repository.statistics.DirectPaymentStatisticsCategoryCount;
import fit.iuh.se.hschat.repository.statistics.DirectPaymentStatisticsCountBucket;
import fit.iuh.se.hschat.repository.statistics.DirectPaymentStatisticsQueryRepository;
import fit.iuh.se.hschat.repository.statistics.DirectPaymentStatisticsSummary;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.statistics.StatisticsCountBucketResponse;
import fit.iuh.se.hsshared.statistics.StatisticsGroupBy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPaymentStatisticsServiceImplTest {

    @Mock
    DirectPaymentStatisticsQueryRepository directRepository;

    @Mock
    CreditPaymentStatisticsQueryRepository creditRepository;

    @InjectMocks
    AdminPaymentStatisticsServiceImpl service;

    @Test
    void mergesBothSourcesAndZeroFillsChartsAndStatuses() {
        Instant from = Instant.parse("2026-09-30T17:00:00Z");
        Instant to = Instant.parse("2026-10-03T17:00:00Z");
        String timezone = "Asia/Ho_Chi_Minh";
        when(directRepository.summarize(from, to))
                .thenReturn(new DirectPaymentStatisticsSummary(1_000, 300, 10, 4));
        when(creditRepository.summarize(from, to))
                .thenReturn(new CreditPaymentStatisticsSummary(2_000, 700, 20, 2));
        when(directRepository.revenueTrend(from, to, StatisticsGroupBy.DAY, timezone)).thenReturn(List.of(
                new DirectPaymentStatisticsAmountBucket(LocalDate.parse("2026-10-01"), 100),
                new DirectPaymentStatisticsAmountBucket(LocalDate.parse("2026-10-03"), 200)));
        when(creditRepository.revenueTrend(from, to, StatisticsGroupBy.DAY, timezone)).thenReturn(List.of(
                new CreditPaymentStatisticsAmountBucket(LocalDate.parse("2026-10-02"), 700)));
        when(directRepository.transactionTrend(from, to, StatisticsGroupBy.DAY, timezone)).thenReturn(List.of(
                new DirectPaymentStatisticsCountBucket(LocalDate.parse("2026-10-01"), 2),
                new DirectPaymentStatisticsCountBucket(LocalDate.parse("2026-10-03"), 1)));
        when(creditRepository.transactionTrend(from, to, StatisticsGroupBy.DAY, timezone)).thenReturn(List.of(
                new CreditPaymentStatisticsCountBucket(LocalDate.parse("2026-10-01"), 3),
                new CreditPaymentStatisticsCountBucket(LocalDate.parse("2026-10-02"), 4)));
        when(directRepository.statusDistribution(from, to)).thenReturn(List.of(
                new DirectPaymentStatisticsCategoryCount("PAID", 2),
                new DirectPaymentStatisticsCategoryCount("FAILED", 1)));
        when(creditRepository.statusDistribution(from, to)).thenReturn(List.of(
                new CreditPaymentStatisticsCategoryCount("PAID", 3),
                new CreditPaymentStatisticsCategoryCount("CANCELLED", 1)));

        var result = service.getStatistics(from, to, StatisticsGroupBy.DAY, timezone);

        assertEquals(3_000, result.summary().totalRevenueVnd());
        assertEquals(1_000, result.summary().revenueInPeriodVnd());
        assertEquals(30, result.summary().totalBusinessTransactions());
        assertEquals(500, result.summary().averagePaidTransactionValueVnd());
        assertEquals(3, result.revenueTrend().size());
        assertEquals(100, result.revenueTrend().get(0).directCareVnd());
        assertEquals(700, result.revenueTrend().get(1).creditPurchaseVnd());
        assertEquals(0, result.revenueTrend().get(2).creditPurchaseVnd());
        assertEquals(List.of(
                        new StatisticsCountBucketResponse(LocalDate.parse("2026-10-01"), 5),
                        new StatisticsCountBucketResponse(LocalDate.parse("2026-10-02"), 4),
                        new StatisticsCountBucketResponse(LocalDate.parse("2026-10-03"), 1)),
                result.transactionTrend());
        assertEquals(6, result.statusDistributionBySource().directCare().size());
        assertEquals(5, result.statusDistributionBySource().creditPurchase().size());
        assertEquals(0, statusCount(result.statusDistributionBySource().directCare(), "PENDING"));
        assertEquals(1, statusCount(result.statusDistributionBySource().creditPurchase(), "CANCELLED"));
        assertEquals(300, result.revenueBySource().get(0).amountVnd());
        assertEquals(700, result.revenueBySource().get(1).amountVnd());
    }

    @Test
    void returnsZeroAverageWhenNoPaidTransactionExists() {
        Instant from = Instant.parse("2026-09-01T00:00:00Z");
        Instant to = Instant.parse("2026-10-01T00:00:00Z");
        stubEmptyRepositories(from, to, StatisticsGroupBy.MONTH, "UTC");

        var result = service.getStatistics(from, to, StatisticsGroupBy.MONTH, "UTC");

        assertEquals(0, result.summary().averagePaidTransactionValueVnd());
        assertEquals(1, result.revenueTrend().size());
        assertEquals(0, result.revenueTrend().getFirst().totalVnd());
    }

    @Test
    void alignsWeeklyBucketsToMonday() {
        Instant from = Instant.parse("2026-09-30T17:00:00Z");
        Instant to = Instant.parse("2026-10-12T17:00:00Z");
        stubEmptyRepositories(from, to, StatisticsGroupBy.WEEK, "Asia/Ho_Chi_Minh");

        var result = service.getStatistics(from, to, StatisticsGroupBy.WEEK, "Asia/Ho_Chi_Minh");

        assertEquals(List.of(
                        LocalDate.parse("2026-09-28"),
                        LocalDate.parse("2026-10-05"),
                        LocalDate.parse("2026-10-12")),
                result.revenueTrend().stream().map(value -> value.bucketStart()).toList());
    }

    @Test
    void rejectsInvalidRangeBeforeQueryingRepositories() {
        Instant instant = Instant.parse("2026-10-01T00:00:00Z");

        assertThrows(AppException.class,
                () -> service.getStatistics(instant, instant, StatisticsGroupBy.DAY, "Asia/Ho_Chi_Minh"));

        verifyNoInteractions(directRepository, creditRepository);
    }

    @Test
    void rejectsRangeLongerThanOneYearBeforeQueryingRepositories() {
        Instant from = Instant.parse("2025-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-01-03T00:00:00Z");

        assertThrows(AppException.class,
                () -> service.getStatistics(from, to, StatisticsGroupBy.MONTH, "Asia/Ho_Chi_Minh"));

        verifyNoInteractions(directRepository, creditRepository);
    }

    @Test
    void rejectsInvalidTimezoneBeforeQueryingRepositories() {
        Instant from = Instant.parse("2026-09-01T00:00:00Z");
        Instant to = Instant.parse("2026-10-01T00:00:00Z");

        assertThrows(AppException.class,
                () -> service.getStatistics(from, to, StatisticsGroupBy.DAY, "Mars/Olympus"));

        verifyNoInteractions(directRepository, creditRepository);
    }

    private void stubEmptyRepositories(
            Instant from,
            Instant to,
            StatisticsGroupBy groupBy,
            String timezone) {
        when(directRepository.summarize(from, to))
                .thenReturn(new DirectPaymentStatisticsSummary(0, 0, 0, 0));
        when(creditRepository.summarize(from, to))
                .thenReturn(new CreditPaymentStatisticsSummary(0, 0, 0, 0));
        when(directRepository.revenueTrend(from, to, groupBy, timezone)).thenReturn(List.of());
        when(creditRepository.revenueTrend(from, to, groupBy, timezone)).thenReturn(List.of());
        when(directRepository.transactionTrend(from, to, groupBy, timezone)).thenReturn(List.of());
        when(creditRepository.transactionTrend(from, to, groupBy, timezone)).thenReturn(List.of());
        when(directRepository.statusDistribution(from, to)).thenReturn(List.of());
        when(creditRepository.statusDistribution(from, to)).thenReturn(List.of());
    }

    private long statusCount(
            List<fit.iuh.se.hsapplication.dto.response.statistics.PaymentStatisticsResponse.CategoryCount> values,
            String key) {
        return values.stream().filter(value -> value.key().equals(key)).findFirst().orElseThrow().count();
    }
}
