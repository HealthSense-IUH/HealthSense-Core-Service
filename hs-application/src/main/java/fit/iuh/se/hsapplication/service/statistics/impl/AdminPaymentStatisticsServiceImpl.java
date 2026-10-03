package fit.iuh.se.hsapplication.service.statistics.impl;

import fit.iuh.se.hsapplication.dto.response.statistics.PaymentStatisticsResponse;
import fit.iuh.se.hsapplication.service.statistics.AdminPaymentStatisticsService;
import fit.iuh.se.hsbilling.entity.enums.CreditOrderStatus;
import fit.iuh.se.hsbilling.repository.statistics.CreditPaymentStatisticsAmountBucket;
import fit.iuh.se.hsbilling.repository.statistics.CreditPaymentStatisticsCountBucket;
import fit.iuh.se.hsbilling.repository.statistics.CreditPaymentStatisticsQueryRepository;
import fit.iuh.se.hschat.entity.enums.ConsultationPaymentStatus;
import fit.iuh.se.hschat.repository.statistics.DirectPaymentStatisticsAmountBucket;
import fit.iuh.se.hschat.repository.statistics.DirectPaymentStatisticsCountBucket;
import fit.iuh.se.hschat.repository.statistics.DirectPaymentStatisticsQueryRepository;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.statistics.StatisticsCountBucketResponse;
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
public class AdminPaymentStatisticsServiceImpl implements AdminPaymentStatisticsService {

    static final long MAX_RANGE_DAYS = 366;

    private final DirectPaymentStatisticsQueryRepository directRepository;
    private final CreditPaymentStatisticsQueryRepository creditRepository;

    @Override
    public PaymentStatisticsResponse getStatistics(
            Instant from,
            Instant to,
            StatisticsGroupBy groupBy,
            String timezone) {
        ZoneId zoneId = validate(from, to, groupBy, timezone);
        var directSummary = directRepository.summarize(from, to);
        var creditSummary = creditRepository.summarize(from, to);

        long totalRevenue = Math.addExact(directSummary.totalRevenueVnd(), creditSummary.totalRevenueVnd());
        long revenueInPeriod = Math.addExact(
                directSummary.revenueInPeriodVnd(),
                creditSummary.revenueInPeriodVnd());
        long totalTransactions = Math.addExact(
                directSummary.totalBusinessTransactions(),
                creditSummary.totalBusinessTransactions());
        long totalPaidTransactions = Math.addExact(
                directSummary.totalPaidTransactions(),
                creditSummary.totalPaidTransactions());

        var directRevenueTrend = directRepository.revenueTrend(from, to, groupBy, zoneId.getId());
        var creditRevenueTrend = creditRepository.revenueTrend(from, to, groupBy, zoneId.getId());
        var directTransactionTrend = directRepository.transactionTrend(from, to, groupBy, zoneId.getId());
        var creditTransactionTrend = creditRepository.transactionTrend(from, to, groupBy, zoneId.getId());

        return new PaymentStatisticsResponse(
                new StatisticsPeriodResponse(from, to, zoneId.getId(), groupBy),
                new PaymentStatisticsResponse.Summary(
                        totalRevenue,
                        revenueInPeriod,
                        totalTransactions,
                        average(totalRevenue, totalPaidTransactions)),
                mergeRevenueTrend(
                        from, to, zoneId, groupBy, directRevenueTrend, creditRevenueTrend),
                mergeTransactionTrend(
                        from, to, zoneId, groupBy, directTransactionTrend, creditTransactionTrend),
                new PaymentStatisticsResponse.StatusDistributionBySource(
                        fillDirectStatuses(directRepository.statusDistribution(from, to)),
                        fillCreditStatuses(creditRepository.statusDistribution(from, to))),
                List.of(
                        new PaymentStatisticsResponse.RevenueBySource(
                                "DIRECT_CARE", directSummary.revenueInPeriodVnd()),
                        new PaymentStatisticsResponse.RevenueBySource(
                                "CREDIT_PURCHASE", creditSummary.revenueInPeriodVnd())));
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

    private long average(long revenueVnd, long paidTransactions) {
        if (paidTransactions == 0)
            return 0L;
        return BigDecimal.valueOf(revenueVnd)
                .divide(BigDecimal.valueOf(paidTransactions), 0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    private List<PaymentStatisticsResponse.RevenueBucket> mergeRevenueTrend(
            Instant from,
            Instant to,
            ZoneId zoneId,
            StatisticsGroupBy groupBy,
            List<DirectPaymentStatisticsAmountBucket> directValues,
            List<CreditPaymentStatisticsAmountBucket> creditValues) {
        Map<LocalDate, SourceAmounts> buckets = createBuckets(from, to, zoneId, groupBy, SourceAmounts::new);
        directValues.forEach(value -> {
            SourceAmounts amounts = buckets.get(value.bucketStart());
            if (amounts != null)
                amounts.directCareVnd = value.amountVnd();
        });
        creditValues.forEach(value -> {
            SourceAmounts amounts = buckets.get(value.bucketStart());
            if (amounts != null)
                amounts.creditPurchaseVnd = value.amountVnd();
        });
        return buckets.entrySet().stream()
                .map(entry -> new PaymentStatisticsResponse.RevenueBucket(
                        entry.getKey(),
                        Math.addExact(entry.getValue().directCareVnd, entry.getValue().creditPurchaseVnd),
                        entry.getValue().directCareVnd,
                        entry.getValue().creditPurchaseVnd))
                .toList();
    }

    private List<StatisticsCountBucketResponse> mergeTransactionTrend(
            Instant from,
            Instant to,
            ZoneId zoneId,
            StatisticsGroupBy groupBy,
            List<DirectPaymentStatisticsCountBucket> directValues,
            List<CreditPaymentStatisticsCountBucket> creditValues) {
        Map<LocalDate, Long> buckets = createBuckets(from, to, zoneId, groupBy, () -> 0L);
        directValues.forEach(value -> buckets.computeIfPresent(
                value.bucketStart(), (key, count) -> Math.addExact(count, value.count())));
        creditValues.forEach(value -> buckets.computeIfPresent(
                value.bucketStart(), (key, count) -> Math.addExact(count, value.count())));
        return buckets.entrySet().stream()
                .map(entry -> new StatisticsCountBucketResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    private <T> Map<LocalDate, T> createBuckets(
            Instant from,
            Instant to,
            ZoneId zoneId,
            StatisticsGroupBy groupBy,
            java.util.function.Supplier<T> initialValue) {
        Map<LocalDate, T> buckets = new LinkedHashMap<>();
        LocalDate current = bucketStart(from.atZone(zoneId).toLocalDate(), groupBy);
        LocalDate last = bucketStart(to.minusNanos(1).atZone(zoneId).toLocalDate(), groupBy);
        while (!current.isAfter(last)) {
            buckets.put(current, initialValue.get());
            current = nextBucket(current, groupBy);
        }
        return buckets;
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

    private List<PaymentStatisticsResponse.CategoryCount> fillDirectStatuses(
            List<fit.iuh.se.hschat.repository.statistics.DirectPaymentStatisticsCategoryCount> values) {
        EnumMap<ConsultationPaymentStatus, Long> counts = new EnumMap<>(ConsultationPaymentStatus.class);
        Arrays.stream(ConsultationPaymentStatus.values()).forEach(status -> counts.put(status, 0L));
        values.forEach(value -> counts.put(ConsultationPaymentStatus.valueOf(value.key()), value.count()));
        return counts.entrySet().stream()
                .map(entry -> new PaymentStatisticsResponse.CategoryCount(entry.getKey().name(), entry.getValue()))
                .toList();
    }

    private List<PaymentStatisticsResponse.CategoryCount> fillCreditStatuses(
            List<fit.iuh.se.hsbilling.repository.statistics.CreditPaymentStatisticsCategoryCount> values) {
        EnumMap<CreditOrderStatus, Long> counts = new EnumMap<>(CreditOrderStatus.class);
        Arrays.stream(CreditOrderStatus.values()).forEach(status -> counts.put(status, 0L));
        values.forEach(value -> counts.put(CreditOrderStatus.valueOf(value.key()), value.count()));
        return counts.entrySet().stream()
                .map(entry -> new PaymentStatisticsResponse.CategoryCount(entry.getKey().name(), entry.getValue()))
                .toList();
    }

    private static final class SourceAmounts {
        private long directCareVnd;
        private long creditPurchaseVnd;
    }
}
