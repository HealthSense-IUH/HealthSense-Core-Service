package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.ConsultationCreditPolicy;

public record ConsultationQueueStatisticsResponse(
        long doctorsOnDuty,
        long availableDoctors,
        long busyDoctors,
        long waitingMembers,
        ConsultationCreditPolicy creditPolicy,
        long creditCost) {
}
