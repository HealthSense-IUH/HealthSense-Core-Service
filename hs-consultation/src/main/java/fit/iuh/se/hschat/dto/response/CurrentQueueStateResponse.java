package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.ConsultationQueueStatus;
import fit.iuh.se.hschat.entity.enums.ConsultationRequestStatus;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.entity.enums.CurrentConsultationPhase;
import fit.iuh.se.hschat.entity.enums.ConsultationCreditPolicy;
import fit.iuh.se.hsbilling.entity.enums.CreditReservationStatus;
import lombok.Builder;

import java.time.Instant;
import java.time.LocalDate;

@Builder
public record CurrentQueueStateResponse(
        CurrentConsultationPhase phase,
        Long requestId,
        Long queueEntryId,
        Long queueNumber,
        LocalDate queueDate,
        ConsultationQueueStatus queueStatus,
        ConsultationRequestStatus requestStatus,
        ConsultationCreditPolicy creditPolicy,
        Long creditCost,
        CreditReservationStatus creditReservationStatus,
        Instant queuedAt,
        long peopleAhead,
        long doctorsOnDuty,
        long availableDoctors,
        long busyDoctors,
        String offerId,
        Instant memberConfirmExpiresAt,
        boolean doctorReady,
        Long doctorId,
        String doctorName,
        Long sessionId,
        ConsultationStatus sessionStatus,
        Instant sessionStartedAt,
        Instant sessionEndsAt) {
}
