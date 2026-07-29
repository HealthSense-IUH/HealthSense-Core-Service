package fit.iuh.se.hschat.entity;

import fit.iuh.se.hschat.entity.enums.ConsultationSourceType;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
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
@Document(collection = "consultation_sessions")
@CompoundIndexes({
        @CompoundIndex(name = "idx_session_member_status", def = "{'member_id': 1, 'status': 1}"),
        @CompoundIndex(name = "idx_session_doctor_status", def = "{'doctor_id': 1, 'status': 1}"),
        @CompoundIndex(name = "idx_session_status_ends_at", def = "{'status': 1, 'ends_at': 1}"),
        @CompoundIndex(name = "idx_session_member_last_message", def = "{'member_id': 1, 'last_message_at': -1}"),
        @CompoundIndex(name = "idx_session_doctor_last_message", def = "{'doctor_id': 1, 'last_message_at': -1}"),
        @CompoundIndex(name = "uq_session_request_id", def = "{'request_id': 1}", unique = true, sparse = true)
})
public class ConsultationSession {

    @Id
    String id;

    @Field("member_id")
    Long memberId;

    @Field("doctor_id")
    Long doctorId;

    @Field("created_by_admin_id")
    Long createdByAdminId;

    @Field("source_type")
    ConsultationSourceType sourceType;

    @Field("status")
    ConsultationStatus status;

    @Field("started_at")
    Instant startedAt;

    @Field("ends_at")
    Instant endsAt;

    @Field("support_ends_at")
    Instant supportEndsAt;

    @Field("closed_at")
    Instant closedAt;

    @Field("close_reason")
    String closeReason;

    @Field("health_record_id")
    Long healthRecordId;

    @Field("request_id")
    String requestId;

    @Field("last_message_id")
    String lastMessageId;

    @Field("last_message_preview")
    String lastMessagePreview;

    @Field("last_message_at")
    Instant lastMessageAt;

    @Field("created_at")
    Instant createdAt;

    @Field("updated_at")
    Instant updatedAt;
}
