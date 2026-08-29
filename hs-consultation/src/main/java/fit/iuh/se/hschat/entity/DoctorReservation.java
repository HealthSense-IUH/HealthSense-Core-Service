package fit.iuh.se.hschat.entity;

import fit.iuh.se.hschat.entity.enums.DoctorReservationReleaseReason;
import fit.iuh.se.hschat.entity.enums.DoctorReservationStatus;
import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "doctor_reservations", indexes = {
        @Index(name = "idx_reservation_request_status", columnList = "request_id, status"),
        @Index(name = "idx_reservation_doctor_capacity", columnList = "doctor_id, status, expires_at")
})
public class DoctorReservation extends BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(nullable = false, updatable = false)
    Long id;

    @Column(name = "request_id", nullable = false)
    Long requestId;

    @Column(name = "doctor_id", nullable = false)
    Long doctorId;

    @Column(name = "package_id")
    Long packageId;

    @Column(name = "package_version")
    Integer packageVersion;

    @Column(name = "reserved_by")
    Long reservedBy;

    @Column(name = "reserved_at", nullable = false)
    Instant reservedAt;

    @Column(name = "expires_at", nullable = false)
    Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    DoctorReservationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "release_reason", length = 50)
    DoctorReservationReleaseReason releaseReason;

    @Column(name = "released_at")
    Instant releasedAt;

    @Version
    @Column(nullable = false)
    long version;
}
