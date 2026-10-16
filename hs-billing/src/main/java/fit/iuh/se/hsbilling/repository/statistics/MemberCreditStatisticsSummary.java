package fit.iuh.se.hsbilling.repository.statistics;

public record MemberCreditStatisticsSummary(
        long totalPaidVnd,
        long paidInPeriodVnd,
        long totalPurchasedCredits,
        long purchasedCreditsInPeriod,
        long totalSuccessfulOrders,
        long successfulOrdersInPeriod,
        long totalOrdersInPeriod) {
}
