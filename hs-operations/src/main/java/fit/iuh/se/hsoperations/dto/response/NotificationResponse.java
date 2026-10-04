package fit.iuh.se.hsoperations.dto.response;

import fit.iuh.se.hsoperations.entity.UserNotification;
import fit.iuh.se.hsoperations.entity.enums.BusinessDomainType;
import fit.iuh.se.hsoperations.entity.enums.NotificationDeliveryStatus;
import fit.iuh.se.hsoperations.entity.enums.NotificationType;
import fit.iuh.se.hsoperations.i18n.NotificationText;

import java.time.Instant;

public record NotificationResponse(Long id, NotificationType type, String title, String message,
                                   BusinessDomainType referenceType, Long referenceId,
                                   NotificationDeliveryStatus deliveryStatus,
                                   Instant createdAt, Instant readAt, boolean read) {
    /** Tiêu đề, nội dung dịch theo ngôn ngữ của request (header {@code lang}), xem {@link NotificationText}. */
    public static NotificationResponse from(UserNotification value) {
        return new NotificationResponse(value.getId(), value.getType(), NotificationText.localize(value.getTitle()),
                NotificationText.localize(value.getMessage()),
                value.getReferenceType(), value.getReferenceId(), value.getDeliveryStatus(), value.getCreatedAt(),
                value.getReadAt(), value.getReadAt() != null);
    }
}
