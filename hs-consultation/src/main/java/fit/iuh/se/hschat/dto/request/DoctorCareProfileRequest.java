package fit.iuh.se.hschat.dto.request;

import fit.iuh.se.hschat.dto.DoctorAvailabilityDto;
import fit.iuh.se.hschat.entity.enums.DoctorSpecialty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorCareProfileRequest {

    @NotNull(message = "Chuyên khoa không được để trống")
    DoctorSpecialty specialty;

    @NotNull(message = "Trạng thái nhận chăm sóc 1-1 không được để trống")
    Boolean acceptsOneOnOneCare;

    @NotNull(message = "Số phiên tối đa không được để trống")
    @Min(value = 1, message = "Số phiên tối đa phải lớn hơn 0")
    Integer maxActiveConsultations;

    @Size(max = 4000, message = "Lịch hỗ trợ không được vượt quá 4000 ký tự")
    String availabilityJson;

    DoctorAvailabilityDto availability;

    @Size(max = 80, message = "Timezone không được vượt quá 80 ký tự")
    String timezone;
}
