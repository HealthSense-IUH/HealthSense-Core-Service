package fit.iuh.se.hschat.service.reservation;

import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;

public class DoctorReservationInvalidException extends AppException {

    public DoctorReservationInvalidException() {
        super(ErrorCode.DOCTOR_NOT_ELIGIBLE_FOR_CONSULTATION,
                "Doctor reservation is no longer valid; the request has returned to coordination");
    }
}
