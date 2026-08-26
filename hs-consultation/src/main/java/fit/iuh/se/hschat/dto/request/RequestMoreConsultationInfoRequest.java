package fit.iuh.se.hschat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestMoreConsultationInfoRequest {

    @NotBlank(message = "Lý do yêu cầu bổ sung thông tin không được để trống")
    @Size(max = 500, message = "Lý do yêu cầu bổ sung thông tin không được vượt quá 500 ký tự")
    String reason;

    @Size(max = 120, message = "Nhóm thông tin yêu cầu không được vượt quá 120 ký tự")
    String requestedItemsCategory;
}
