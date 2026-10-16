package fit.iuh.se.hsbilling.dto.statistics;

import fit.iuh.se.hsbilling.dto.CreditWalletResponse;
import fit.iuh.se.hsshared.statistics.StatisticsPeriodResponse;

import java.time.LocalDate;
import java.util.List;

public record MemberPaymentStatisticsResponse(
        StatisticsPeriodResponse period,
        Summary summary,
        CreditWalletResponse wallet,
        List<PaymentBucket> paymentTrend,
        List<CategoryCount> orderStatusDistribution) {

    public record Summary(
            long totalPaidVnd,
            long paidInPeriodVnd,
            long totalPurchasedCredits,
            long purchasedCreditsInPeriod,
            long totalSuccessfulOrders,
            long successfulOrdersInPeriod,
            long totalOrdersInPeriod,
            long averagePaidOrderValueInPeriodVnd) {
    }

    public record PaymentBucket(
            LocalDate bucketStart,
            long paidVnd,
            long purchasedCredits,
            long successfulOrders) {
    }

    public record CategoryCount(String key, long count) {
    }
}
