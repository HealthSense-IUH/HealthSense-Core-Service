package fit.iuh.se.hschat.entity;

import fit.iuh.se.hschat.entity.enums.ConsultationParticipantRole;
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
@Document(collection = "consultation_participants")
@CompoundIndexes({
        @CompoundIndex(name = "uq_participant_session_user", def = "{'session_id': 1, 'user_id': 1}", unique = true),
        @CompoundIndex(name = "idx_participant_user", def = "{'user_id': 1}"),
        @CompoundIndex(name = "idx_participant_session", def = "{'session_id': 1}"),
        @CompoundIndex(name = "idx_participant_user_active", def = "{'user_id': 1, 'active': 1}")
})
public class ConsultationParticipant {

    @Id
    String id;

    @Field("session_id")
    String sessionId;

    @Field("user_id")
    Long userId;

    @Field("role")
    ConsultationParticipantRole role;

    @Field("last_read_message_id")
    String lastReadMessageId;

    @Field("last_read_at")
    Instant lastReadAt;

    @Field("joined_at")
    Instant joinedAt;

    @Field("left_at")
    Instant leftAt;

    @Field("active")
    Boolean active;

    @Field("created_at")
    Instant createdAt;

    @Field("updated_at")
    Instant updatedAt;
}
