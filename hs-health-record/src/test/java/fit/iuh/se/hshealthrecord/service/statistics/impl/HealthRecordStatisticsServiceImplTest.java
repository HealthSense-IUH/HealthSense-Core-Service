package fit.iuh.se.hshealthrecord.service.statistics.impl;

import fit.iuh.se.hshealthrecord.repository.statistics.HealthRecordStatisticsBucket;
import fit.iuh.se.hshealthrecord.repository.statistics.HealthRecordStatisticsCategoryCount;
import fit.iuh.se.hshealthrecord.repository.statistics.HealthRecordStatisticsHourCount;
import fit.iuh.se.hshealthrecord.repository.statistics.HealthRecordStatisticsQueryRepository;
import fit.iuh.se.hshealthrecord.repository.statistics.HealthRecordStatisticsSummary;
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
class HealthRecordStatisticsServiceImplTest {

    @Mock
    HealthRecordStatisticsQueryRepository repository;

    @InjectMocks
    HealthRecordStatisticsServiceImpl service;

    @Test
    void returnsSummaryAndZeroFillsTimelineResultsAndHours() {
        Instant from = Instant.parse("2026-09-30T17:00:00Z");
        Instant to = Instant.parse("2026-10-03T17:00:00Z");
        when(repository.summarize(from, to))
                .thenReturn(new HealthRecordStatisticsSummary(9860, 720, 318, 64));
        when(repository.recordTrend(from, to, StatisticsGroupBy.DAY, "Asia/Ho_Chi_Minh"))
                .thenReturn(List.of(
                        new HealthRecordStatisticsBucket(LocalDate.parse("2026-10-01"), 22),
                        new HealthRecordStatisticsBucket(LocalDate.parse("2026-10-03"), 27)));
        when(repository.resultDistribution(from, to)).thenReturn(List.of(
                new HealthRecordStatisticsCategoryCount("NORMAL", 601),
                new HealthRecordStatisticsCategoryCount("AFIB", 21)));
        when(repository.recordedHourDistribution(from, to, "Asia/Ho_Chi_Minh")).thenReturn(List.of(
                new HealthRecordStatisticsHourCount(0, 5),
                new HealthRecordStatisticsHourCount(8, 56)));

        var result = service.getStatistics(from, to, StatisticsGroupBy.DAY, "Asia/Ho_Chi_Minh");

        assertEquals(9860, result.summary().totalRecords());
        assertEquals(720, result.summary().recordsInPeriod());
        assertEquals(318, result.summary().uniqueUsersInPeriod());
        assertEquals(64, result.summary().afDetectedOrSuspectedInPeriod());
        assertEquals(List.of(
                        new StatisticsCountBucketResponse(LocalDate.parse("2026-10-01"), 22),
                        new StatisticsCountBucketResponse(LocalDate.parse("2026-10-02"), 0),
                        new StatisticsCountBucketResponse(LocalDate.parse("2026-10-03"), 27)),
                result.recordTrend());
        assertEquals(4, result.resultDistribution().size());
        assertEquals(0, categoryCount(result, "UNCERTAIN"));
        assertEquals(24, result.recordedHourDistribution().size());
        assertEquals(56, result.recordedHourDistribution().get(8).count());
        assertEquals(0, result.recordedHourDistribution().get(23).count());
    }

    @Test
    void alignsWeeklyBucketsToMonday() {
        Instant from = Instant.parse("2026-09-30T17:00:00Z");
        Instant to = Instant.parse("2026-10-12T17:00:00Z");
        stubEmptyRepository(from, to, StatisticsGroupBy.WEEK, "Asia/Ho_Chi_Minh");

        var result = service.getStatistics(from, to, StatisticsGroupBy.WEEK, "Asia/Ho_Chi_Minh");

        assertEquals(List.of(
                        LocalDate.parse("2026-09-28"),
                        LocalDate.parse("2026-10-05"),
                        LocalDate.parse("2026-10-12")),
                result.recordTrend().stream().map(StatisticsCountBucketResponse::bucketStart).toList());
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

    private void stubEmptyRepository(
            Instant from,
            Instant to,
            StatisticsGroupBy groupBy,
            String timezone) {
        when(repository.summarize(from, to)).thenReturn(new HealthRecordStatisticsSummary(0, 0, 0, 0));
        when(repository.recordTrend(from, to, groupBy, timezone)).thenReturn(List.of());
        when(repository.resultDistribution(from, to)).thenReturn(List.of());
        when(repository.recordedHourDistribution(from, to, timezone)).thenReturn(List.of());
    }

    private long categoryCount(
            fit.iuh.se.hshealthrecord.dto.response.statistics.HealthRecordStatisticsResponse response,
            String key) {
        return response.resultDistribution().stream()
                .filter(value -> value.key().equals(key))
                .findFirst()
                .orElseThrow()
                .count();
    }
}
