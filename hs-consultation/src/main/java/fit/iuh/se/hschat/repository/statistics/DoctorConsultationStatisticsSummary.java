package fit.iuh.se.hschat.repository.statistics;

public record DoctorConsultationStatisticsSummary(
        long totalConsultations,
        long consultationsInPeriod,
        long scheduledInPeriod,
        long activeInPeriod,
        long completedInPeriod,
        long cancelledInPeriod) {
}
