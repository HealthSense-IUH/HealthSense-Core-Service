package fit.iuh.se.hsoperations.dto.response;

import fit.iuh.se.hsoperations.entity.BusinessAuditEvent;
import fit.iuh.se.hsoperations.entity.enums.*;
import java.time.Instant;

public record BusinessAuditEventResponse(Long id, BusinessDomainType domainType, Long domainId,
        BusinessEventType eventType, BusinessActorType actorType, Long actorUserId, String actorRole,
        Long requestId, Long agreementId, Long paymentId, Long sessionId, Long renewalId, Long refundId,
        Long healthRecordId, Long memberId, Long doctorId, Long summaryId, String previousState,
        String newState, String reason, String metadataJson, Long correctionOfEventId, Instant occurredAt) {
    public static BusinessAuditEventResponse from(BusinessAuditEvent e) {
        return new BusinessAuditEventResponse(e.getId(), e.getDomainType(), e.getDomainId(), e.getEventType(),
                e.getActorType(), e.getActorUserId(), e.getActorRole(), e.getRequestId(), e.getAgreementId(),
                e.getPaymentId(), e.getSessionId(), e.getRenewalId(), e.getRefundId(), e.getHealthRecordId(),
                e.getMemberId(), e.getDoctorId(), e.getSummaryId(), e.getPreviousState(), e.getNewState(),
                e.getReason(), e.getMetadataJson(), e.getCorrectionOfEventId(), e.getOccurredAt());
    }
}
