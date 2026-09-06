package fit.iuh.se.hschat.service.dispatch.event;

import fit.iuh.se.hschat.entity.enums.DoctorDispatchStatus;

import java.time.Instant;

public record DoctorDispatchStatusChanged(
        Long doctorId,
        DoctorDispatchStatus previousStatus,
        DoctorDispatchStatus currentStatus,
        Instant occurredAt) {
}
