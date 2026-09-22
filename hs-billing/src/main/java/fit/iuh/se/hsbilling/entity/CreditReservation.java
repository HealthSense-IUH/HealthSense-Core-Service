package fit.iuh.se.hsbilling.entity;

import fit.iuh.se.hsbilling.entity.enums.*;
import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "credit_reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreditReservation extends BaseEntity {
    @Id @SnowflakeGenerated
    @Column(nullable = false, updatable = false) Long id;
    @Column(name = "wallet_id", nullable = false) Long walletId;
    @Column(name = "request_id", nullable = false, unique = true) Long requestId;
    @Column(nullable = false) long quantity;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) CreditReservationStatus status;
    @Column(name = "session_id", unique = true) Long sessionId;
    @Column(name = "held_at", nullable = false) java.time.Instant heldAt;
    @Column(name = "captured_at") java.time.Instant capturedAt;
    @Column(name = "released_at") java.time.Instant releasedAt;
}
