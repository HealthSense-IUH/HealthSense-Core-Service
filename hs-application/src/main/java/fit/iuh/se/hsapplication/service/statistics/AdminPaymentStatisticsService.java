package fit.iuh.se.hsapplication.service.statistics;

import fit.iuh.se.hsapplication.dto.response.statistics.PaymentStatisticsResponse;
import fit.iuh.se.hsshared.statistics.StatisticsGroupBy;

import java.time.Instant;

public interface AdminPaymentStatisticsService {

    PaymentStatisticsResponse getStatistics(
            Instant from,
            Instant to,
            StatisticsGroupBy groupBy,
            String timezone);
}
