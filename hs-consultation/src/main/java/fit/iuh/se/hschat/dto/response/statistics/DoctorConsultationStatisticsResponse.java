package fit.iuh.se.hschat.dto.response.statistics;

import java.math.BigDecimal;

public record DoctorConsultationStatisticsResponse(
        DoctorStatisticsPeriodResponse period,
        Summary summary) {

    public record Summary(
            long totalConsultations,
            long consultationsInPeriod,
            long scheduledInPeriod,
            long activeInPeriod,
            long completedInPeriod,
            long cancelledInPeriod,
            BigDecimal completionRate,
            BigDecimal cancellationRate) {
    }
}
