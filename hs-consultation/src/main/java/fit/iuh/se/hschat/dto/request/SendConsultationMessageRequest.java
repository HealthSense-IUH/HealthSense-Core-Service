package fit.iuh.se.hschat.dto.request;

import fit.iuh.se.hschat.entity.enums.ConsultationMessageType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SendConsultationMessageRequest {

    @NotNull(message = "{validation.message-type-required}")
    ConsultationMessageType type;

    @Size(max = 4000, message = "{validation.message-content-max-4000}")
    String content;

    @Size(max = 1000, message = "{validation.attachment-url-max-1000}")
    String attachmentUrl;

    @Size(max = 255, message = "{validation.attachment-file-name-max-255}")
    String attachmentName;

    @Positive(message = "{validation.attachment-file-size-positive}")
    Long attachmentSize;

    @Size(max = 100, message = "{validation.attachment-content-type-max-100}")
    String attachmentContentType;

    @Size(max = 100, message = "{validation.client-message-id-max-100}")
    String clientMessageId;
}
