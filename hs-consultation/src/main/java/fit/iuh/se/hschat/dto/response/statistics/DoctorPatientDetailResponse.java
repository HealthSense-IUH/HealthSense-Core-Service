package fit.iuh.se.hschat.dto.response.statistics;

import java.time.LocalDate;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

public record DoctorPatientDetailResponse(
        @JsonSerialize(using = ToStringSerializer.class)
        Long memberId,
        String email,
        String displayName,
        String phone,
        LocalDate dateOfBirth,
        String gender,
        String avatarUrl,
        String address,
        String timezone,
        String healthInsuranceNumber) {
}
