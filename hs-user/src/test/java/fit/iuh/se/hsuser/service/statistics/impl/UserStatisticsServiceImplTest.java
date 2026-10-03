package fit.iuh.se.hsuser.service.statistics.impl;

import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.statistics.StatisticsGroupBy;
import fit.iuh.se.hsuser.repository.statistics.UserStatisticsBucket;
import fit.iuh.se.hsuser.repository.statistics.UserStatisticsCategoryCount;
import fit.iuh.se.hsuser.repository.statistics.UserStatisticsQueryRepository;
import fit.iuh.se.hsuser.repository.statistics.UserStatisticsSummary;
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
class UserStatisticsServiceImplTest {

    @Mock
    UserStatisticsQueryRepository repository;

    @InjectMocks
    UserStatisticsServiceImpl service;

    @Test
    void returnsSummaryAndZeroFillsTimelineAndDistributions() {
        Instant from = Instant.parse("2026-09-30T17:00:00Z");
        Instant to = Instant.parse("2026-10-03T17:00:00Z");
        when(repository.summarize(from, to)).thenReturn(new UserStatisticsSummary(120, 7, 108, 9));
        when(repository.registrationTrend(from, to, StatisticsGroupBy.DAY, "Asia/Ho_Chi_Minh"))
                .thenReturn(List.of(
                        new UserStatisticsBucket(LocalDate.parse("2026-10-01"), 2),
                        new UserStatisticsBucket(LocalDate.parse("2026-10-03"), 5)));
        when(repository.roleDistribution()).thenReturn(List.of(
                new UserStatisticsCategoryCount("MEMBER", 100),
                new UserStatisticsCategoryCount("DOCTOR", 9)));
        when(repository.statusDistribution()).thenReturn(List.of(
                new UserStatisticsCategoryCount("ACTIVE", 108),
                new UserStatisticsCategoryCount("INACTIVE", 12)));

        var result = service.getStatistics(from, to, StatisticsGroupBy.DAY, "Asia/Ho_Chi_Minh");

        assertEquals(120, result.summary().totalUsers());
        assertEquals(7, result.summary().newUsers());
        assertEquals(108, result.summary().activeAccounts());
        assertEquals(9, result.summary().totalDoctors());
        assertEquals(List.of(
                        new fit.iuh.se.hsshared.statistics.StatisticsCountBucketResponse(LocalDate.parse("2026-10-01"), 2),
                        new fit.iuh.se.hsshared.statistics.StatisticsCountBucketResponse(LocalDate.parse("2026-10-02"), 0),
                        new fit.iuh.se.hsshared.statistics.StatisticsCountBucketResponse(LocalDate.parse("2026-10-03"), 5)),
                result.registrationTrend());
        assertEquals(5, result.byRole().size());
        assertEquals(0, count(result.byRole(), "SUPER_ADMIN"));
        assertEquals(100, count(result.byRole(), "MEMBER"));
        assertEquals(3, result.byStatus().size());
        assertEquals(0, count(result.byStatus(), "PENDING_VERIFY"));
    }

    @Test
    void alignsWeeklyBucketsToMonday() {
        Instant from = Instant.parse("2026-09-30T17:00:00Z");
        Instant to = Instant.parse("2026-10-12T17:00:00Z");
        when(repository.summarize(from, to)).thenReturn(new UserStatisticsSummary(0, 0, 0, 0));
        when(repository.registrationTrend(from, to, StatisticsGroupBy.WEEK, "Asia/Ho_Chi_Minh"))
                .thenReturn(List.of());
        when(repository.roleDistribution()).thenReturn(List.of());
        when(repository.statusDistribution()).thenReturn(List.of());

        var result = service.getStatistics(from, to, StatisticsGroupBy.WEEK, "Asia/Ho_Chi_Minh");

        assertEquals(List.of(
                        LocalDate.parse("2026-09-28"),
                        LocalDate.parse("2026-10-05"),
                        LocalDate.parse("2026-10-12")),
                result.registrationTrend().stream().map(bucket -> bucket.bucketStart()).toList());
    }

    @Test
    void rejectsInvalidRangeBeforeQueryingRepository() {
        Instant instant = Instant.parse("2026-10-01T00:00:00Z");

        assertThrows(AppException.class,
                () -> service.getStatistics(instant, instant, StatisticsGroupBy.DAY, "Asia/Ho_Chi_Minh"));

        verifyNoInteractions(repository);
    }

    @Test
    void rejectsRangeLongerThanOneYearBeforeQueryingRepository() {
        Instant from = Instant.parse("2025-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-01-03T00:00:00Z");

        assertThrows(AppException.class,
                () -> service.getStatistics(from, to, StatisticsGroupBy.MONTH, "Asia/Ho_Chi_Minh"));

        verifyNoInteractions(repository);
    }

    @Test
    void rejectsInvalidTimezoneBeforeQueryingRepository() {
        Instant from = Instant.parse("2026-09-01T00:00:00Z");
        Instant to = Instant.parse("2026-10-01T00:00:00Z");

        assertThrows(AppException.class,
                () -> service.getStatistics(from, to, StatisticsGroupBy.DAY, "Mars/Olympus"));

        verifyNoInteractions(repository);
    }

    private long count(
            List<fit.iuh.se.hsuser.dto.response.statistics.UserStatisticsResponse.CategoryCount> values,
            String key) {
        return values.stream().filter(value -> value.key().equals(key)).findFirst().orElseThrow().count();
    }
}
