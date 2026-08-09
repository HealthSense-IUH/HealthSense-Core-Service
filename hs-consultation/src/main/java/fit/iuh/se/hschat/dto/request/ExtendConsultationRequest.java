package fit.iuh.se.hschat.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExtendConsultationRequest {

    @NotNull(message = "Thời điểm hết hạn tư vấn mới không được để trống")
    @Future(message = "Thời điểm hết hạn tư vấn mới phải ở tương lai")
    Instant endsAt;

    @Future(message = "Thời điểm hết hỗ trợ mới phải ở tương lai")
    Instant supportEndsAt;

    @Size(max = 500, message = "Lý do gia hạn không được vượt quá 500 ký tự")
    String reason;
}
