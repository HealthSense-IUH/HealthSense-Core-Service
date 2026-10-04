package fit.iuh.se.hsuser.repository.statistics;

public record UserStatisticsSummary(
        long totalUsers,
        long newUsers,
        long activeAccounts,
        long totalDoctors) {
}
