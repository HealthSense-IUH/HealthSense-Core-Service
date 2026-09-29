package fit.iuh.se.hschat.dto.request;

import fit.iuh.se.hschat.dto.DoctorAvailabilityDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateDoctorAvailabilityRequest(
        @NotNull(message = "{validation.availability-required}")
        DoctorAvailabilityDto availability,

        @Size(max = 80, message = "{validation.timezone-max-80}")
        String timezone
) {
}
