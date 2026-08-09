package fit.iuh.se.hschat.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminCreateConsultationSessionRequest {

    @NotNull(message = "Id bệnh nhân không được để trống")
    Long memberId;

    @NotNull(message = "Id bác sĩ không được để trống")
    Long doctorId;

    Long packageId;

    Long healthRecordId;

    Instant startedAt;

    @NotNull(message = "Thời điểm hết hạn tư vấn không được để trống")
    @Future(message = "Thời điểm hết hạn tư vấn phải ở tương lai")
    Instant endsAt;

    @Future(message = "Thời điểm hết hỗ trợ phải ở tương lai")
    Instant supportEndsAt;

    String initialSystemMessage;
}
