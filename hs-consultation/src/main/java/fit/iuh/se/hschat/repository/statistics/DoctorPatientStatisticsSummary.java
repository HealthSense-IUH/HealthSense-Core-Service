package fit.iuh.se.hschat.repository.statistics;

public record DoctorPatientStatisticsSummary(
        long totalPatients,
        long patientsInPeriod,
        long newPatientsInPeriod,
        long returningPatientsInPeriod,
        long consultationsInPeriod) {
}
