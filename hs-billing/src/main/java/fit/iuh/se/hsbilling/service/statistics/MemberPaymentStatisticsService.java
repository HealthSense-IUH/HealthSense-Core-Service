package fit.iuh.se.hsbilling.service.statistics;

import fit.iuh.se.hsbilling.dto.statistics.MemberPaymentStatisticsResponse;
import fit.iuh.se.hsshared.statistics.StatisticsGroupBy;

import java.time.Instant;

public interface MemberPaymentStatisticsService {

    MemberPaymentStatisticsResponse getStatistics(
            Long memberId,
            Instant from,
            Instant to,
            StatisticsGroupBy groupBy,
            String timezone);
}
