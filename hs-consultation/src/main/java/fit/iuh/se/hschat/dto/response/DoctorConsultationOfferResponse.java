package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.service.dispatch.offer.DoctorOfferState;
import java.time.Instant;

public record DoctorConsultationOfferResponse(
        String offerId,
        Long queueEntryId,
        Long requestId,
        DoctorOfferState state,
        Instant offeredAt,
        Instant doctorOfferExpiresAt,
        Instant doctorAcceptedAt,
        Instant memberConfirmExpiresAt,
        MinimalMemberIntakeContext minimalMemberIntakeContext) {
}
