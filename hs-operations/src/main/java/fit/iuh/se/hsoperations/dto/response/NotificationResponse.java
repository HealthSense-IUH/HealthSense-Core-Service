package fit.iuh.se.hsoperations.dto.response;

import fit.iuh.se.hsoperations.entity.UserNotification;
import fit.iuh.se.hsoperations.entity.enums.*;
import java.time.Instant;

public record NotificationResponse(Long id, NotificationType type, String title, String message,
        BusinessDomainType referenceType, Long referenceId, NotificationDeliveryStatus deliveryStatus,
        Instant createdAt, Instant readAt, boolean read) {
    public static NotificationResponse from(UserNotification value) {
        return new NotificationResponse(value.getId(), value.getType(), value.getTitle(), value.getMessage(),
                value.getReferenceType(), value.getReferenceId(), value.getDeliveryStatus(), value.getCreatedAt(),
                value.getReadAt(), value.getReadAt() != null);
    }
}
