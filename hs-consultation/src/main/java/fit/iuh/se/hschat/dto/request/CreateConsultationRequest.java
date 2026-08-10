package fit.iuh.se.hschat.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateConsultationRequest {

    @NotNull(message = "Gói chăm sóc không được để trống")
    Long packageId;

    Long healthRecordId;

    @NotBlank(message = "Lý do yêu cầu tư vấn không được để trống")
    @Size(max = 1000, message = "Lý do yêu cầu tư vấn không được vượt quá 1000 ký tự")
    String reason;

    Long preferredDoctorId;
}
