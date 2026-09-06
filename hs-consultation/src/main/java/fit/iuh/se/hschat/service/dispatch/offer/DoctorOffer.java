package fit.iuh.se.hschat.service.dispatch.offer;

import java.time.Instant;

public record DoctorOffer(
        String offerId,
        Long queueEntryId,
        Long requestId,
        Long memberId,
        Long doctorId,
        DoctorOfferState state,
        Instant offeredAt,
        Instant doctorOfferExpiresAt,
        Instant doctorAcceptedAt,
        Instant memberConfirmExpiresAt) {
}
