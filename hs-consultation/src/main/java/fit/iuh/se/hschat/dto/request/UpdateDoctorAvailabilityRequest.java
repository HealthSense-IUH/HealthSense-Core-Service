package fit.iuh.se.hschat.dto.request;

import fit.iuh.se.hschat.dto.DoctorAvailabilityDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateDoctorAvailabilityRequest(
        @NotNull(message = "Lịch hỗ trợ không được để trống")
        DoctorAvailabilityDto availability,

        @Size(max = 80, message = "Timezone không được vượt quá 80 ký tự")
        String timezone
) {
}
