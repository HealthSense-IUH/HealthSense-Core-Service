package fit.iuh.se.hshealthrecord.service.statistics.impl;

import fit.iuh.se.hshealthrecord.dto.response.statistics.HealthRecordStatisticsResponse;
import fit.iuh.se.hshealthrecord.entity.enums.PredictionLabel;
import fit.iuh.se.hshealthrecord.repository.statistics.HealthRecordStatisticsBucket;
import fit.iuh.se.hshealthrecord.repository.statistics.HealthRecordStatisticsQueryRepository;
import fit.iuh.se.hshealthrecord.service.statistics.HealthRecordStatisticsService;
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
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HealthRecordStatisticsServiceImpl implements HealthRecordStatisticsService {

    static final long MAX_RANGE_DAYS = 366;

    private final HealthRecordStatisticsQueryRepository repository;

    @Override
    public HealthRecordStatisticsResponse getStatistics(
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
                repository.recordTrend(from, to, groupBy, zoneId.getId()));

        return new HealthRecordStatisticsResponse(
                new StatisticsPeriodResponse(from, to, zoneId.getId(), groupBy),
                new HealthRecordStatisticsResponse.Summary(
                        summary.totalRecords(),
                        summary.recordsInPeriod(),
                        summary.uniqueUsersInPeriod(),
                        summary.afDetectedOrSuspectedInPeriod()),
                trend,
                fillResultDistribution(repository.resultDistribution(from, to)),
                fillHourDistribution(repository.recordedHourDistribution(from, to, zoneId.getId())));
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
            List<HealthRecordStatisticsBucket> values) {
        Map<LocalDate, Long> counts = new LinkedHashMap<>();
        LocalDate current = bucketStart(from.atZone(zoneId).toLocalDate(), groupBy);
        LocalDate last = bucketStart(to.minusNanos(1).atZone(zoneId).toLocalDate(), groupBy);
        while (!current.isAfter(last)) {
            counts.put(current, 0L);
            current = nextBucket(current, groupBy);
        }
        for (HealthRecordStatisticsBucket value : values) {
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

    private List<HealthRecordStatisticsResponse.CategoryCount> fillResultDistribution(
            List<fit.iuh.se.hshealthrecord.repository.statistics.HealthRecordStatisticsCategoryCount> values) {
        EnumMap<PredictionLabel, Long> counts = new EnumMap<>(PredictionLabel.class);
        Arrays.stream(PredictionLabel.values()).forEach(label -> counts.put(label, 0L));
        values.forEach(value -> counts.put(PredictionLabel.valueOf(value.key()), value.count()));
        return counts.entrySet().stream()
                .map(entry -> new HealthRecordStatisticsResponse.CategoryCount(entry.getKey().name(), entry.getValue()))
                .toList();
    }

    private List<HealthRecordStatisticsResponse.HourCount> fillHourDistribution(
            List<fit.iuh.se.hshealthrecord.repository.statistics.HealthRecordStatisticsHourCount> values) {
        Map<Integer, Long> counts = new LinkedHashMap<>();
        IntStream.range(0, 24).forEach(hour -> counts.put(hour, 0L));
        values.forEach(value -> counts.put(value.hour(), value.count()));
        return counts.entrySet().stream()
                .map(entry -> new HealthRecordStatisticsResponse.HourCount(entry.getKey(), entry.getValue()))
                .toList();
    }
}
