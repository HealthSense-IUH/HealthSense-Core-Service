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

    @NotBlank(message = "Id tin nhắn đã đọc không được để trống")
    String lastReadMessageId;
}
