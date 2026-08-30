package fit.iuh.se.hsoperations.dto.command;

import fit.iuh.se.hsoperations.entity.enums.*;

public record NeedsActionIntent(
        NeedsActionType type,
        NeedsActionPriority priority,
        String title,
        String description,
        BusinessDomainType referenceType,
        Long referenceId,
        String assignedRole,
        String idempotencyKey) {
}
