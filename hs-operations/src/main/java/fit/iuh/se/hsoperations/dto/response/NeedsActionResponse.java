package fit.iuh.se.hsoperations.dto.response;

import fit.iuh.se.hsoperations.entity.NeedsActionItem;
import fit.iuh.se.hsoperations.entity.enums.*;
import java.time.Instant;

public record NeedsActionResponse(Long id, NeedsActionType type, NeedsActionStatus status,
        NeedsActionPriority priority, String title, String description, BusinessDomainType referenceType,
        Long referenceId, Long requestId, Long paymentId, Long sessionId, Long renewalId, Long refundId,
        Long memberId, Long doctorId, String assignedRole, Long claimedBy, Instant claimedAt,
        Long resolvedBy, Instant resolvedAt, String resolution, Instant createdAt, Instant updatedAt) {
    public static NeedsActionResponse from(NeedsActionItem i) {
        return new NeedsActionResponse(i.getId(), i.getType(), i.getStatus(), i.getPriority(), i.getTitle(),
                i.getDescription(), i.getReferenceType(), i.getReferenceId(), i.getRequestId(), i.getPaymentId(),
                i.getSessionId(), i.getRenewalId(), i.getRefundId(), i.getMemberId(), i.getDoctorId(),
                i.getAssignedRole(), i.getClaimedBy(), i.getClaimedAt(), i.getResolvedBy(), i.getResolvedAt(),
                i.getResolution(), i.getCreatedAt(), i.getUpdatedAt());
    }
}
