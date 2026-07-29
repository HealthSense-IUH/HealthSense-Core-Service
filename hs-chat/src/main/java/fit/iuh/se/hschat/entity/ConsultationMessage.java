package fit.iuh.se.hschat.entity;

import fit.iuh.se.hschat.entity.enums.ConsultationMessageType;
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
@Document(collection = "consultation_messages")
@CompoundIndexes({
        @CompoundIndex(name = "idx_message_session_created_at", def = "{'session_id': 1, 'created_at': -1}"),
        @CompoundIndex(name = "idx_message_session_active", def = "{'session_id': 1, 'active': 1}"),
        @CompoundIndex(
                name = "uq_message_client_id",
                def = "{'session_id': 1, 'sender_id': 1, 'client_message_id': 1}",
                unique = true,
                sparse = true
        )
})
public class ConsultationMessage {

    @Id
    String id;

    @Field("session_id")
    String sessionId;

    @Field("sender_id")
    Long senderId;

    @Field("sender_role")
    ConsultationParticipantRole senderRole;

    @Field("type")
    ConsultationMessageType type;

    @Field("content")
    String content;

    @Field("attachment_url")
    String attachmentUrl;

    @Field("attachment_name")
    String attachmentName;

    @Field("attachment_size")
    Long attachmentSize;

    @Field("attachment_content_type")
    String attachmentContentType;

    @Field("client_message_id")
    String clientMessageId;

    @Field("active")
    Boolean active;

    @Field("created_at")
    Instant createdAt;

    @Field("updated_at")
    Instant updatedAt;
}
