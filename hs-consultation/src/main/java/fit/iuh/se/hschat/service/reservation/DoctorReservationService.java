package fit.iuh.se.hschat.service.reservation;

import fit.iuh.se.hschat.entity.ConsultationRequest;
import fit.iuh.se.hschat.entity.DoctorReservation;
import fit.iuh.se.hschat.entity.enums.DoctorIneligibilityReason;
import fit.iuh.se.hschat.entity.enums.DoctorReservationReleaseReason;
import fit.iuh.se.hsuser.entity.UserAccount;

import java.time.Instant;
import java.util.List;

public interface DoctorReservationService {

    DoctorReservation reserve(ConsultationRequest request, Long coordinatorId, Long doctorId, Instant expiresAt);

    long getEffectiveLoad(Long doctorId, Instant now);

    List<DoctorIneligibilityReason> getIneligibilityReasons(
            ConsultationRequest request,
            UserAccount doctor,
            Instant now,
            Long ownReservationId
    );

    boolean revalidateBeforePayment(ConsultationRequest request);

    boolean revalidateBeforeActivation(ConsultationRequest request);

    void release(ConsultationRequest request, DoctorReservationReleaseReason reason);

    void expireOverdueReservations(Instant now);
}
