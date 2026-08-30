package fit.iuh.se.hschat.entity;

import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Immutable
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "consultation_session_extensions", indexes = {
        @Index(name = "idx_session_extension_session_applied", columnList = "session_id, applied_at")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_session_extension_renewal", columnNames = "renewal_id"),
        @UniqueConstraint(name = "uq_session_extension_payment", columnNames = "payment_id")
})
public class SessionExtension extends BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(nullable = false, updatable = false)
    Long id;

    @Column(name = "session_id", nullable = false, updatable = false)
    Long sessionId;

    @Column(name = "renewal_id", nullable = false, updatable = false)
    Long renewalId;

    @Column(name = "agreement_id", nullable = false, updatable = false)
    Long agreementId;

    @Column(name = "payment_id", nullable = false, updatable = false)
    Long paymentId;

    @Column(name = "previous_ends_at", nullable = false, updatable = false)
    Instant previousEndsAt;

    @Column(name = "new_ends_at", nullable = false, updatable = false)
    Instant newEndsAt;

    @Column(name = "duration_days", nullable = false, updatable = false)
    Integer durationDays;

    @Column(name = "package_id", nullable = false, updatable = false)
    Long packageId;

    @Column(name = "package_version", nullable = false, updatable = false)
    Integer packageVersion;

    @Column(name = "price_amount", nullable = false, updatable = false, precision = 14, scale = 2)
    BigDecimal priceAmount;

    @Column(name = "currency", nullable = false, updatable = false, length = 3)
    String currency;

    @Column(name = "support_schedule_snapshot_json", nullable = false, updatable = false, columnDefinition = "TEXT")
    String supportScheduleSnapshotJson;

    @Column(name = "support_timezone_snapshot", nullable = false, updatable = false, length = 80)
    String supportTimezoneSnapshot;

    @Column(name = "applied_at", nullable = false, updatable = false)
    Instant appliedAt;
}
