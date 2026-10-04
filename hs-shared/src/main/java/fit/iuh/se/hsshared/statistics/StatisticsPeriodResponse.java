package fit.iuh.se.hsshared.statistics;

import java.time.Instant;

public record StatisticsPeriodResponse(
        Instant from,
        Instant to,
        String timezone,
        StatisticsGroupBy groupBy) {
}
