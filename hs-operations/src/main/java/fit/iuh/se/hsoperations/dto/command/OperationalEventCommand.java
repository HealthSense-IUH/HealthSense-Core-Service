package fit.iuh.se.hsoperations.dto.command;

import fit.iuh.se.hsoperations.entity.enums.*;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Builder
public record OperationalEventCommand(
        BusinessDomainType domainType,
        Long domainId,
        BusinessEventType eventType,
        BusinessActorType actorType,
        Long actorUserId,
        String actorRole,
        Long requestId,
        Long agreementId,
        Long paymentId,
        Long sessionId,
        Long renewalId,
        Long refundId,
        Long healthRecordId,
        Long memberId,
        Long doctorId,
        Long summaryId,
        String previousState,
        String newState,
        String reason,
        Map<String, String> metadata,
        Long correctionOfEventId,
        String idempotencyKey,
        Instant occurredAt,
        List<NotificationIntent> notifications,
        NeedsActionIntent needsAction) {
}
