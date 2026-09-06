package fit.iuh.se.hschat.service.dispatch.offer;

public enum DoctorOfferState {
    OFFERED_TO_DOCTOR,
    WAITING_MEMBER_CONFIRMATION,
    CONFIRMED,
    DOCTOR_REJECTED,
    DOCTOR_TIMEOUT,
    MEMBER_TIMEOUT,
    CANCELLED
}
