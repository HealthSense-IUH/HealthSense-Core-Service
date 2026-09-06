package fit.iuh.se.hschat.service.dispatch;

import fit.iuh.se.hschat.dto.response.ConsultationDispatchCandidate;
import fit.iuh.se.hschat.entity.ConsultationDispatchState;
import fit.iuh.se.hschat.entity.ConsultationQueueEntry;
import fit.iuh.se.hschat.entity.DoctorCareProfile;

import java.util.Optional;

public interface DoctorDispatchSelectionService {

    Optional<DoctorCareProfile> previewNextEligibleDoctor();

    Optional<ConsultationQueueEntry> previewNextWaitingQueueEntry();

    Optional<ConsultationDispatchCandidate> inspectNextDispatchCandidate();

    long countEffectivelyDispatchableDoctors();

    ConsultationDispatchState lockDispatchStateForOfferCommit();

    Optional<DoctorCareProfile> lockAndRevalidateEligibleDoctor(Long doctorId);

    void advanceLockedPointerAfterOfferCommit(ConsultationDispatchState lockedState, Long doctorId);
}
