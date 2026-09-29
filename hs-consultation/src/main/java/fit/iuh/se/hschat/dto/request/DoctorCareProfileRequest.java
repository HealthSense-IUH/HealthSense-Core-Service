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

    @NotNull(message = "{validation.specialty-required}")
    DoctorSpecialty specialty;

    @NotNull(message = "{validation.accepts-one-on-one-care-required}")
    Boolean acceptsOneOnOneCare;

    @NotNull(message = "{validation.max-active-consultations-required}")
    @Min(value = 1, message = "{validation.max-active-consultations-positive}")
    Integer maxActiveConsultations;

    @Size(max = 4000, message = "{validation.availability-max-4000}")
    String availabilityJson;

    DoctorAvailabilityDto availability;

    @Size(max = 80, message = "{validation.timezone-max-80}")
    String timezone;
}
