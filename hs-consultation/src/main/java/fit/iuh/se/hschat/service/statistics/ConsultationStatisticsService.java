package fit.iuh.se.hschat.service.statistics;

import fit.iuh.se.hschat.dto.response.statistics.ConsultationStatisticsResponse;
import fit.iuh.se.hsshared.statistics.StatisticsGroupBy;

import java.time.Instant;

public interface ConsultationStatisticsService {

    ConsultationStatisticsResponse getStatistics(
            Instant from,
            Instant to,
            StatisticsGroupBy groupBy,
            String timezone);
}
