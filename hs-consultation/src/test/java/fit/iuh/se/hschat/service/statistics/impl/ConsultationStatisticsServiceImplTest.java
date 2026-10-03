package fit.iuh.se.hschat.service.statistics.impl;

import fit.iuh.se.hschat.repository.statistics.ConsultationStatisticsBucket;
import fit.iuh.se.hschat.repository.statistics.ConsultationStatisticsCategoryCount;
import fit.iuh.se.hschat.repository.statistics.ConsultationStatisticsQueryRepository;
import fit.iuh.se.hschat.repository.statistics.ConsultationStatisticsSummary;
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
class ConsultationStatisticsServiceImplTest {

    @Mock
    ConsultationStatisticsQueryRepository repository;

    @InjectMocks
    ConsultationStatisticsServiceImpl service;

    @Test
    void returnsSummaryAndZeroFillsTimelineAndStatuses() {
        Instant from = Instant.parse("2026-09-30T17:00:00Z");
        Instant to = Instant.parse("2026-10-03T17:00:00Z");
        when(repository.summarize(from, to))
                .thenReturn(new ConsultationStatisticsSummary(80, 9, 5, 1));
        when(repository.consultationTrend(from, to, StatisticsGroupBy.DAY, "Asia/Ho_Chi_Minh"))
                .thenReturn(List.of(
                        new ConsultationStatisticsBucket(LocalDate.parse("2026-10-01"), 4),
                        new ConsultationStatisticsBucket(LocalDate.parse("2026-10-03"), 5)));
        when(repository.statusDistribution(from, to)).thenReturn(List.of(
                new ConsultationStatisticsCategoryCount("ACTIVE", 3),
                new ConsultationStatisticsCategoryCount("COMPLETED", 5),
                new ConsultationStatisticsCategoryCount("CANCELLED", 1)));

        var result = service.getStatistics(from, to, StatisticsGroupBy.DAY, "Asia/Ho_Chi_Minh");

        assertEquals(80, result.summary().totalConsultations());
        assertEquals(9, result.summary().consultationsInPeriod());
        assertEquals(5, result.summary().completedInPeriod());
        assertEquals(1, result.summary().cancelledInPeriod());
        assertEquals(List.of(
                        new StatisticsCountBucketResponse(LocalDate.parse("2026-10-01"), 4),
                        new StatisticsCountBucketResponse(LocalDate.parse("2026-10-02"), 0),
                        new StatisticsCountBucketResponse(LocalDate.parse("2026-10-03"), 5)),
                result.consultationTrend());
        assertEquals(4, result.byStatus().size());
        assertEquals(0, count(result.byStatus(), "SCHEDULED"));
        assertEquals(3, count(result.byStatus(), "ACTIVE"));
    }

    @Test
    void alignsWeeklyBucketsToMonday() {
        Instant from = Instant.parse("2026-09-30T17:00:00Z");
        Instant to = Instant.parse("2026-10-12T17:00:00Z");
        when(repository.summarize(from, to))
                .thenReturn(new ConsultationStatisticsSummary(0, 0, 0, 0));
        when(repository.consultationTrend(from, to, StatisticsGroupBy.WEEK, "Asia/Ho_Chi_Minh"))
                .thenReturn(List.of());
        when(repository.statusDistribution(from, to)).thenReturn(List.of());

        var result = service.getStatistics(from, to, StatisticsGroupBy.WEEK, "Asia/Ho_Chi_Minh");

        assertEquals(List.of(
                        LocalDate.parse("2026-09-28"),
                        LocalDate.parse("2026-10-05"),
                        LocalDate.parse("2026-10-12")),
                result.consultationTrend().stream().map(StatisticsCountBucketResponse::bucketStart).toList());
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
            List<fit.iuh.se.hschat.dto.response.statistics.ConsultationStatisticsResponse.CategoryCount> values,
            String key) {
        return values.stream().filter(value -> value.key().equals(key)).findFirst().orElseThrow().count();
    }
}
