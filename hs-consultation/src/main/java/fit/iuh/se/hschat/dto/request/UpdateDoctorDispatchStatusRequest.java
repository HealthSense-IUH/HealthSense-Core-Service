package fit.iuh.se.hschat.dto.request;

import fit.iuh.se.hschat.entity.enums.DoctorDispatchStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateDoctorDispatchStatusRequest(
        @NotNull DoctorDispatchStatus dispatchStatus) {
}
