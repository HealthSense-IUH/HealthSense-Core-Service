package fit.iuh.se.hschat.entity;

import fit.iuh.se.hschat.entity.enums.ConsultationRequestStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "consultation_requests")
@CompoundIndexes({
        @CompoundIndex(name = "idx_request_member_status", def = "{'member_id': 1, 'status': 1}"),
        @CompoundIndex(name = "idx_request_status_created_at", def = "{'status': 1, 'created_at': -1}"),
        @CompoundIndex(name = "idx_request_assigned_doctor", def = "{'assigned_doctor_id': 1}"),
        @CompoundIndex(name = "idx_request_health_record", def = "{'health_record_id': 1}", sparse = true),
        @CompoundIndex(
                name = "uq_request_consultation_session",
                def = "{'consultation_session_id': 1}",
                unique = true,
                sparse = true
        )
})
public class ConsultationRequest {

    @Id
    String id;

    @Field("member_id")
    Long memberId;

    @Field("health_record_id")
    Long healthRecordId;

    @Field("reason")
    String reason;

    @Field("preferred_doctor_id")
    Long preferredDoctorId;

    @Field("status")
    ConsultationRequestStatus status;

    @Field("assigned_doctor_id")
    Long assignedDoctorId;

    @Field("consultation_session_id")
    String consultationSessionId;

    @Field("reviewed_by_admin_id")
    Long reviewedByAdminId;

    @Field("reviewed_at")
    Instant reviewedAt;

    @Field("rejection_reason")
    String rejectionReason;

    @Field("created_at")
    Instant createdAt;

    @Field("updated_at")
    Instant updatedAt;
}
