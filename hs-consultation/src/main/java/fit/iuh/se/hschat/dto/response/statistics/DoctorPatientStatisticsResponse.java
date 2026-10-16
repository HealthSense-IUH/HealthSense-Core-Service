package fit.iuh.se.hschat.dto.response.statistics;

import java.math.BigDecimal;

public record DoctorPatientStatisticsResponse(
        DoctorStatisticsPeriodResponse period,
        Summary summary) {

    public record Summary(
            long totalPatients,
            long patientsInPeriod,
            long newPatientsInPeriod,
            long returningPatientsInPeriod,
            BigDecimal averageConsultationsPerPatientInPeriod) {
    }
}
