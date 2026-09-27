package fit.iuh.se.hsoperations.dto.command;

import fit.iuh.se.hsoperations.entity.enums.BusinessDomainType;
import fit.iuh.se.hsoperations.entity.enums.NeedsActionPriority;
import fit.iuh.se.hsoperations.entity.enums.NeedsActionType;

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
