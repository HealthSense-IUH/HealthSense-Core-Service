package fit.iuh.se.hsbilling.service.statistics.impl;

import fit.iuh.se.hsbilling.dto.statistics.MemberPaymentStatisticsResponse;
import fit.iuh.se.hsbilling.entity.enums.CreditOrderStatus;
import fit.iuh.se.hsbilling.repository.statistics.MemberCreditStatisticsBucket;
import fit.iuh.se.hsbilling.repository.statistics.MemberCreditStatisticsQueryRepository;
import fit.iuh.se.hsbilling.service.ConsultationCreditService;
import fit.iuh.se.hsbilling.service.statistics.MemberPaymentStatisticsService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.statistics.StatisticsGroupBy;
import fit.iuh.se.hsshared.statistics.StatisticsPeriodResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
public class MemberPaymentStatisticsServiceImpl implements MemberPaymentStatisticsService {

    static final long MAX_RANGE_DAYS = 366;

    private final MemberCreditStatisticsQueryRepository repository;
    private final ConsultationCreditService credits;

    @Override
    public MemberPaymentStatisticsResponse getStatistics(
            Long memberId,
            Instant from,
            Instant to,
            StatisticsGroupBy groupBy,
            String timezone) {
        if (memberId == null || memberId <= 0) throw new AppException(ErrorCode.UNAUTHORIZED);
        ZoneId zoneId = validate(from, to, groupBy, timezone);
        var wallet = credits.getWallet(memberId);
        var summary = repository.summarize(memberId, from, to);
        long average = summary.successfulOrdersInPeriod() == 0 ? 0
                : BigDecimal.valueOf(summary.paidInPeriodVnd())
                .divide(BigDecimal.valueOf(summary.successfulOrdersInPeriod()), 0, RoundingMode.HALF_UP)
                .longValueExact();

        return new MemberPaymentStatisticsResponse(
                new StatisticsPeriodResponse(from, to, zoneId.getId(), groupBy),
                new MemberPaymentStatisticsResponse.Summary(
                        summary.totalPaidVnd(),
                        summary.paidInPeriodVnd(),
                        summary.totalPurchasedCredits(),
                        summary.purchasedCreditsInPeriod(),
                        summary.totalSuccessfulOrders(),
                        summary.successfulOrdersInPeriod(),
                        summary.totalOrdersInPeriod(),
                        average),
                wallet,
                fillBuckets(from, to, zoneId, groupBy,
                        repository.paymentTrend(memberId, from, to, groupBy, zoneId.getId())),
                fillStatuses(repository.statusDistribution(memberId, from, to)));
    }

    private ZoneId validate(Instant from, Instant to, StatisticsGroupBy groupBy, String timezone) {
        if (from == null) throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.statistics-from-required");
        if (to == null) throw AppException.of(ErrorCode.INVALID_PARAMETER, "detail.statistics-to-required");
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

    private List<MemberPaymentStatisticsResponse.PaymentBucket> fillBuckets(
            Instant from,
            Instant to,
            ZoneId zoneId,
            StatisticsGroupBy groupBy,
            List<MemberCreditStatisticsBucket> values) {
        Map<LocalDate, MemberCreditStatisticsBucket> buckets = new LinkedHashMap<>();
        LocalDate current = bucketStart(from.atZone(zoneId).toLocalDate(), groupBy);
        LocalDate last = bucketStart(to.minusNanos(1).atZone(zoneId).toLocalDate(), groupBy);
        while (!current.isAfter(last)) {
            buckets.put(current, new MemberCreditStatisticsBucket(current, 0, 0, 0));
            current = nextBucket(current, groupBy);
        }
        values.forEach(value -> {
            if (value.bucketStart() != null && buckets.containsKey(value.bucketStart()))
                buckets.put(value.bucketStart(), value);
        });
        return buckets.values().stream()
                .map(value -> new MemberPaymentStatisticsResponse.PaymentBucket(
                        value.bucketStart(), value.paidVnd(), value.purchasedCredits(), value.successfulOrders()))
                .toList();
    }

    private List<MemberPaymentStatisticsResponse.CategoryCount> fillStatuses(
            List<fit.iuh.se.hsbilling.repository.statistics.CreditPaymentStatisticsCategoryCount> values) {
        EnumMap<CreditOrderStatus, Long> counts = new EnumMap<>(CreditOrderStatus.class);
        Arrays.stream(CreditOrderStatus.values()).forEach(status -> counts.put(status, 0L));
        values.forEach(value -> counts.put(CreditOrderStatus.valueOf(value.key()), value.count()));
        return counts.entrySet().stream()
                .map(entry -> new MemberPaymentStatisticsResponse.CategoryCount(entry.getKey().name(), entry.getValue()))
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
}
