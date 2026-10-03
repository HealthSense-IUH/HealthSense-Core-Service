package fit.iuh.se.hschat.service.statistics.impl;

import fit.iuh.se.hschat.dto.response.statistics.ConsultationStatisticsResponse;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.repository.statistics.ConsultationStatisticsBucket;
import fit.iuh.se.hschat.repository.statistics.ConsultationStatisticsCategoryCount;
import fit.iuh.se.hschat.repository.statistics.ConsultationStatisticsQueryRepository;
import fit.iuh.se.hschat.service.statistics.ConsultationStatisticsService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.statistics.StatisticsCountBucketResponse;
import fit.iuh.se.hsshared.statistics.StatisticsGroupBy;
import fit.iuh.se.hsshared.statistics.StatisticsPeriodResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultationStatisticsServiceImpl implements ConsultationStatisticsService {

    static final long MAX_RANGE_DAYS = 366;

    private final ConsultationStatisticsQueryRepository repository;

    @Override
    public ConsultationStatisticsResponse getStatistics(
            Instant from,
            Instant to,
            StatisticsGroupBy groupBy,
            String timezone) {
        ZoneId zoneId = validate(from, to, groupBy, timezone);
        var summary = repository.summarize(from, to);
        var trend = fillMissingBuckets(
                from,
                to,
                zoneId,
                groupBy,
                repository.consultationTrend(from, to, groupBy, zoneId.getId()));
        var statuses = fillStatusDistribution(repository.statusDistribution(from, to));

        return new ConsultationStatisticsResponse(
                new StatisticsPeriodResponse(from, to, zoneId.getId(), groupBy),
                new ConsultationStatisticsResponse.Summary(
                        summary.totalConsultations(),
                        summary.consultationsInPeriod(),
                        summary.completedInPeriod(),
                        summary.cancelledInPeriod()),
                trend,
                statuses);
    }

    private ZoneId validate(Instant from, Instant to, StatisticsGroupBy groupBy, String timezone) {
        if (from == null)
            throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.statistics-from-required");
        if (to == null)
            throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.statistics-to-required");
        if (!from.isBefore(to))
            throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.from-date-before-to-date");
        if (Duration.between(from, to).compareTo(Duration.ofDays(MAX_RANGE_DAYS)) > 0)
            throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.statistics-range-max-days", MAX_RANGE_DAYS);
        if (groupBy == null)
            throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.statistics-group-by-required");
        try {
            return ZoneId.of(timezone == null || timezone.isBlank() ? "Asia/Ho_Chi_Minh" : timezone.trim());
        } catch (DateTimeException exception) {
            throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.statistics-timezone-invalid", timezone);
        }
    }

    private List<StatisticsCountBucketResponse> fillMissingBuckets(
            Instant from,
            Instant to,
            ZoneId zoneId,
            StatisticsGroupBy groupBy,
            List<ConsultationStatisticsBucket> values) {
        Map<LocalDate, Long> counts = new LinkedHashMap<>();
        LocalDate current = bucketStart(from.atZone(zoneId).toLocalDate(), groupBy);
        LocalDate last = bucketStart(to.minusNanos(1).atZone(zoneId).toLocalDate(), groupBy);
        while (!current.isAfter(last)) {
            counts.put(current, 0L);
            current = nextBucket(current, groupBy);
        }
        for (ConsultationStatisticsBucket value : values) {
            if (value.bucketStart() != null && counts.containsKey(value.bucketStart()))
                counts.put(value.bucketStart(), value.count());
        }
        return counts.entrySet().stream()
                .map(entry -> new StatisticsCountBucketResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    private LocalDate bucketStart(LocalDate date, StatisticsGroupBy groupBy) {
        return switch (groupBy) {
            case DAY -> date;
            case WEEK -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTH -> YearMonth.from(date).atDay(1);
        };
    }

    private LocalDate nextBucket(LocalDate current, StatisticsGroupBy groupBy) {
        return switch (groupBy) {
            case DAY -> current.plusDays(1);
            case WEEK -> current.plusWeeks(1);
            case MONTH -> current.plusMonths(1);
        };
    }

    private List<ConsultationStatisticsResponse.CategoryCount> fillStatusDistribution(
            List<ConsultationStatisticsCategoryCount> values) {
        EnumMap<ConsultationStatus, Long> counts = new EnumMap<>(ConsultationStatus.class);
        Arrays.stream(ConsultationStatus.values()).forEach(status -> counts.put(status, 0L));
        values.forEach(value -> counts.put(ConsultationStatus.valueOf(value.key()), value.count()));
        return counts.entrySet().stream()
                .map(entry -> new ConsultationStatisticsResponse.CategoryCount(entry.getKey().name(), entry.getValue()))
                .toList();
    }
}
