package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.ConsultationMessageType;
import fit.iuh.se.hschat.entity.enums.ConsultationParticipantRole;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConsultationMessageResponse {

    String id;
    String sessionId;
    Long senderId;
    ConsultationParticipantRole senderRole;
    ConsultationMessageType type;
    String content;
    String attachmentUrl;
    String attachmentName;
    Long attachmentSize;
    String attachmentContentType;
    Boolean active;
    Instant createdAt;
    Instant updatedAt;
}
