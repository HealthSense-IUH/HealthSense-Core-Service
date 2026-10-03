package fit.iuh.se.hshealthrecord.service.statistics;

import fit.iuh.se.hshealthrecord.dto.response.statistics.HealthRecordStatisticsResponse;
import fit.iuh.se.hsshared.statistics.StatisticsGroupBy;

import java.time.Instant;

public interface HealthRecordStatisticsService {

    HealthRecordStatisticsResponse getStatistics(
            Instant from,
            Instant to,
            StatisticsGroupBy groupBy,
            String timezone);
}
