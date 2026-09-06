package fit.iuh.se.hschat.entity;

import fit.iuh.se.hschat.entity.enums.ConsultationQueueStatus;
import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "consultation_queue_entries", indexes = {
        @Index(name = "idx_queue_fifo", columnList = "status, queue_date, queue_number"),
        @Index(name = "idx_queue_member_status", columnList = "member_id, status")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uq_queue_entry_request", columnNames = "request_id"),
        @UniqueConstraint(name = "uq_queue_entry_daily_number", columnNames = {"queue_date", "queue_number"})
})
public class ConsultationQueueEntry extends BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(nullable = false, updatable = false)
    Long id;

    @Column(name = "request_id", nullable = false, updatable = false)
    Long requestId;

    @Column(name = "member_id", nullable = false, updatable = false)
    Long memberId;

    @Column(name = "queue_date", nullable = false, updatable = false)
    LocalDate queueDate;

    @Column(name = "queue_number", nullable = false, updatable = false)
    Long queueNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    ConsultationQueueStatus status;

    @Column(name = "queued_at", nullable = false, updatable = false)
    Instant queuedAt;

    @Column(name = "cancelled_at")
    Instant cancelledAt;

    @Column(name = "timed_out_at")
    Instant timedOutAt;

    @Column(name = "fulfilled_at")
    Instant fulfilledAt;

    @Version
    @Column(nullable = false)
    long version;
}
