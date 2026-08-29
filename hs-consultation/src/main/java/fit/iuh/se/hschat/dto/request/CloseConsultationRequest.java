package fit.iuh.se.hschat.dto.request;

import fit.iuh.se.hschat.entity.enums.CareTerminationReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CloseConsultationRequest {

    @NotBlank(message = "Lý do đóng phiên tư vấn không được để trống")
    @Size(max = 500, message = "Lý do đóng phiên tư vấn không được vượt quá 500 ký tự")
    String closeReason;

    CareTerminationReason terminationReason;

    Boolean meaningfulCareOccurred;
}
