package fit.iuh.se.hschat.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MarkConsultationReadRequest {

    @NotBlank(message = "{validation.last-read-message-id-required}")
    String lastReadMessageId;
}
