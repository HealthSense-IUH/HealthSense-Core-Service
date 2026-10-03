package fit.iuh.se.hschat.repository.statistics;

public record ConsultationStatisticsSummary(
        long totalConsultations,
        long consultationsInPeriod,
        long completedInPeriod,
        long cancelledInPeriod) {
}
