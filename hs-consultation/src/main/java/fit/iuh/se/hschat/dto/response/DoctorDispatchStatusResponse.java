package fit.iuh.se.hschat.dto.response;

import fit.iuh.se.hschat.entity.enums.DoctorDispatchStatus;

import java.time.Instant;

public record DoctorDispatchStatusResponse(
        Long doctorId,
        DoctorDispatchStatus dispatchStatus,
        boolean effectivelyDispatchable,
        boolean stopAfterCurrentSession,
        Long busySessionId,
        Instant dispatchStatusChangedAt) {
}
