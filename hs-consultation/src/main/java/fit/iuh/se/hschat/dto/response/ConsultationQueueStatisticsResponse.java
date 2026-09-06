package fit.iuh.se.hschat.dto.response;

public record ConsultationQueueStatisticsResponse(
        long doctorsOnDuty,
        long availableDoctors,
        long busyDoctors,
        long waitingMembers) {
}
