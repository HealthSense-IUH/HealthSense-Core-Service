package fit.iuh.se.hschat.entity;

import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "consultation_more_info_cycles", indexes = {
        @Index(name = "idx_more_info_cycle_request", columnList = "request_id, requested_at")
})
public class ConsultationMoreInfoCycle extends BaseEntity {

    @Id
    @SnowflakeGenerated
    @Column(name = "id", nullable = false, updatable = false)
    Long id;

    @Column(name = "request_id", nullable = false)
    Long requestId;

    @Column(name = "requested_items_category", length = 120)
    String requestedItemsCategory;

    @Column(name = "coordinator_message", nullable = false, length = 1000)
    String coordinatorMessage;

    @Column(name = "requested_by", nullable = false)
    Long requestedBy;

    @Column(name = "requested_at", nullable = false)
    Instant requestedAt;

    @Column(name = "member_response", length = 2000)
    String memberResponse;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "consultation_more_info_response_records",
            joinColumns = @JoinColumn(name = "cycle_id")
    )
    @Column(name = "health_record_id", nullable = false)
    @OrderColumn(name = "reference_order")
    @Builder.Default
    List<Long> responseHealthRecordIds = new ArrayList<>();

    @Column(name = "responded_at")
    Instant respondedAt;
}
