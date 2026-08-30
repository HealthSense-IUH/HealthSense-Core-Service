package fit.iuh.se.hsoperations.dto.command;

import fit.iuh.se.hsoperations.entity.enums.BusinessDomainType;
import fit.iuh.se.hsoperations.entity.enums.NotificationType;
import fit.iuh.se.hsuser.entity.enums.UserRole;

public record NotificationIntent(
        Long recipientId,
        UserRole recipientRole,
        NotificationType type,
        String title,
        String message,
        BusinessDomainType referenceType,
        Long referenceId,
        String idempotencyKey) {

    public NotificationIntent(
            Long recipientId,
            NotificationType type,
            String title,
            String message,
            BusinessDomainType referenceType,
            Long referenceId,
            String idempotencyKey) {
        this(recipientId, null, type, title, message, referenceType, referenceId, idempotencyKey);
    }

    public static NotificationIntent forRole(
            UserRole recipientRole,
            NotificationType type,
            String title,
            String message,
            BusinessDomainType referenceType,
            Long referenceId,
            String idempotencyKey) {
        return new NotificationIntent(null, recipientRole, type, title, message,
                referenceType, referenceId, idempotencyKey);
    }
}
