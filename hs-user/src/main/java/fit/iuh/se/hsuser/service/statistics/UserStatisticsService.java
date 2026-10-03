package fit.iuh.se.hsuser.service.statistics;

import fit.iuh.se.hsshared.statistics.StatisticsGroupBy;
import fit.iuh.se.hsuser.dto.response.statistics.UserStatisticsResponse;

import java.time.Instant;

public interface UserStatisticsService {

    UserStatisticsResponse getStatistics(
            Instant from,
            Instant to,
            StatisticsGroupBy groupBy,
            String timezone);
}
