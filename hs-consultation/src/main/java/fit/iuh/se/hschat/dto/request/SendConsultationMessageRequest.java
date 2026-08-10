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

    @NotNull(message = "Loại tin nhắn không được để trống")
    ConsultationMessageType type;

    @Size(max = 4000, message = "Nội dung tin nhắn không được vượt quá 4000 ký tự")
    String content;

    @Size(max = 1000, message = "URL file đính kèm không được vượt quá 1000 ký tự")
    String attachmentUrl;

    @Size(max = 255, message = "Tên file đính kèm không được vượt quá 255 ký tự")
    String attachmentName;

    @Positive(message = "Dung lượng file đính kèm phải lớn hơn 0")
    Long attachmentSize;

    @Size(max = 100, message = "Content type file đính kèm không được vượt quá 100 ký tự")
    String attachmentContentType;

    @Size(max = 100, message = "Client message id không được vượt quá 100 ký tự")
    String clientMessageId;
}
