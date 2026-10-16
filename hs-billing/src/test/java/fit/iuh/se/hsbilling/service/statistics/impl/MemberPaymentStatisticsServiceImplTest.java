package fit.iuh.se.hsbilling.service.statistics.impl;

import fit.iuh.se.hsbilling.dto.CreditWalletResponse;
import fit.iuh.se.hsbilling.repository.statistics.CreditPaymentStatisticsCategoryCount;
import fit.iuh.se.hsbilling.repository.statistics.MemberCreditStatisticsBucket;
import fit.iuh.se.hsbilling.repository.statistics.MemberCreditStatisticsQueryRepository;
import fit.iuh.se.hsbilling.repository.statistics.MemberCreditStatisticsSummary;
import fit.iuh.se.hsbilling.service.ConsultationCreditService;
import fit.iuh.se.hsshared.advice.entity.AppException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberPaymentStatisticsServiceImplTest {

    @Mock MemberCreditStatisticsQueryRepository repository;
    @Mock ConsultationCreditService credits;
    @InjectMocks MemberPaymentStatisticsServiceImpl service;

    @Test
    void returnsMemberPaymentStatisticsAndZeroFillsCharts() {
        Instant from = Instant.parse("2026-09-30T17:00:00Z");
        Instant to = Instant.parse("2026-10-03T17:00:00Z");
        when(credits.getWallet(123L)).thenReturn(new CreditWalletResponse(20, 2, 18));
        when(repository.summarize(123L, from, to))
                .thenReturn(new MemberCreditStatisticsSummary(500_000, 150_001, 50, 15, 6, 2, 3));
        when(repository.paymentTrend(123L, from, to, StatisticsGroupBy.DAY, "Asia/Ho_Chi_Minh"))
                .thenReturn(List.of(new MemberCreditStatisticsBucket(
                        LocalDate.parse("2026-10-02"), 150_001, 15, 2)));
        when(repository.statusDistribution(123L, from, to)).thenReturn(List.of(
                new CreditPaymentStatisticsCategoryCount("PAID", 2),
                new CreditPaymentStatisticsCategoryCount("CANCELLED", 1)));

        var result = service.getStatistics(123L, from, to, StatisticsGroupBy.DAY, "Asia/Ho_Chi_Minh");

        assertEquals(500_000, result.summary().totalPaidVnd());
        assertEquals(15, result.summary().purchasedCreditsInPeriod());
        assertEquals(75_001, result.summary().averagePaidOrderValueInPeriodVnd());
        assertEquals(18, result.wallet().available());
        assertEquals(3, result.paymentTrend().size());
        assertEquals(150_001, result.paymentTrend().get(1).paidVnd());
        assertEquals(5, result.orderStatusDistribution().size());
    }

    @Test
    void rejectsInvalidRangeBeforeReadingWalletOrRepository() {
        Instant instant = Instant.parse("2026-10-01T00:00:00Z");

        assertThrows(AppException.class,
                () -> service.getStatistics(123L, instant, instant, StatisticsGroupBy.DAY, "Asia/Ho_Chi_Minh"));

        verifyNoInteractions(repository, credits);
    }
}
