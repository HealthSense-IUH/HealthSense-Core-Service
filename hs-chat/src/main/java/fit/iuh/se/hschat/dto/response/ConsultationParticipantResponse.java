package fit.iuh.se.hschat.dto.response;

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
public class ConsultationParticipantResponse {

    String id;
    String sessionId;
    Long userId;
    ConsultationParticipantRole role;
    String lastReadMessageId;
    Instant lastReadAt;
    Instant joinedAt;
    Boolean active;
}
