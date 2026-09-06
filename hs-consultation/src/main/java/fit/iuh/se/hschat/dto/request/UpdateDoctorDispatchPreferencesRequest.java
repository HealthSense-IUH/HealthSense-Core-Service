package fit.iuh.se.hschat.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateDoctorDispatchPreferencesRequest(
        @NotNull Boolean stopAfterCurrentSession) {
}
