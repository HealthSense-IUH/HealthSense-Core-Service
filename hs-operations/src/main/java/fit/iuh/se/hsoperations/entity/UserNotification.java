package fit.iuh.se.hsoperations.entity;

import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Table(name = "user_notifications", indexes = {
        @Index(name = "idx_notification_recipient_created", columnList = "recipient_id, created_at"),
        @Index(name = "idx_notification_recipient_read", columnList = "recipient_id, read_at"),
        @Index(name = "idx_notification_reference", columnList = "reference_type, reference_id")
}, uniqueConstraints = @UniqueConstraint(name = "uq_notification_idempotency", columnNames = "idempotency_key"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserNotification extends BaseEntity {
    @Id @SnowflakeGenerated @Column(nullable = false, updatable = false) Long id;
    @Column(name = "recipient_id", nullable = false, updatable = false) Long recipientId;
    @Enumerated(EnumType.STRING) @Column(name = "type", nullable = false, updatable = false, length = 50) NotificationType type;
    @Column(name = "title", nullable = false, updatable = false, length = 200) String title;
    @Column(name = "message", nullable = false, updatable = false, length = 1000) String message;
    @Enumerated(EnumType.STRING) @Column(name = "reference_type", updatable = false, length = 40) BusinessDomainType referenceType;
    @Column(name = "reference_id", updatable = false) Long referenceId;
    @Column(name = "idempotency_key", nullable = false, updatable = false, length = 255) String idempotencyKey;
    @Enumerated(EnumType.STRING) @Column(name = "delivery_status", nullable = false, length = 20) NotificationDeliveryStatus deliveryStatus;
    @Column(name = "delivery_error", length = 1000) String deliveryError;
    @Column(name = "read_at") Instant readAt;
}
