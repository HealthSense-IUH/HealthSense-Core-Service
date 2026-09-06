package fit.iuh.se.hschat.service.dispatch.impl;

import fit.iuh.se.hschat.dto.response.ConsultationDispatchCandidate;
import fit.iuh.se.hschat.entity.ConsultationDispatchState;
import fit.iuh.se.hschat.entity.ConsultationQueueEntry;
import fit.iuh.se.hschat.entity.DoctorCareProfile;
import fit.iuh.se.hschat.entity.enums.ConsultationQueueStatus;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.entity.enums.DoctorDispatchStatus;
import fit.iuh.se.hschat.repository.ConsultationDispatchStateRepository;
import fit.iuh.se.hschat.repository.ConsultationQueueEntryRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.repository.DoctorCareProfileRepository;
import fit.iuh.se.hschat.service.dispatch.DoctorDispatchSelectionService;
import fit.iuh.se.hschat.service.dispatch.offer.DoctorOfferStore;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DoctorDispatchSelectionServiceImpl implements DoctorDispatchSelectionService {

    static final List<ConsultationStatus> INCOMPATIBLE_SESSION_STATUSES = List.of(
            ConsultationStatus.SCHEDULED,
            ConsultationStatus.ACTIVE);

    DoctorCareProfileRepository profileRepository;
    ConsultationQueueEntryRepository queueEntryRepository;
    ConsultationDispatchStateRepository dispatchStateRepository;
    ConsultationSessionRepository sessionRepository;
    UserAccountRepository userAccountRepository;
    DoctorOfferStore offerStore;

    @Override
    @Transactional(readOnly = true)
    public Optional<DoctorCareProfile> previewNextEligibleDoctor() {
        List<DoctorCareProfile> eligible = effectivelyDispatchableDoctors();
        if (eligible.isEmpty()) return Optional.empty();

        Long lastDoctorId = dispatchStateRepository.findById(ConsultationDispatchState.SINGLETON_ID)
                .map(ConsultationDispatchState::getLastDoctorId)
                .orElse(null);
        if (lastDoctorId == null)
            return Optional.of(eligible.getFirst());
        return eligible.stream()
                .filter(profile -> profile.getDoctorId() > lastDoctorId)
                .findFirst()
                .or(() -> Optional.of(eligible.getFirst()));
    }

    @Override
    @Transactional(readOnly = true)
    public long countEffectivelyDispatchableDoctors() {
        return effectivelyDispatchableDoctors().size();
    }

    private List<DoctorCareProfile> effectivelyDispatchableDoctors() {
        List<DoctorCareProfile> available = profileRepository
                .findByDispatchStatusOrderByDoctorIdAsc(DoctorDispatchStatus.AVAILABLE);
        if (available.isEmpty()) return List.of();

        Set<Long> activeDoctorIds = new HashSet<>();
        userAccountRepository.findByIdIn(available.stream().map(DoctorCareProfile::getDoctorId).toList())
                .stream()
                .filter(account -> account.getRole() == UserRole.DOCTOR
                        && account.getStatus() == AccountStatus.ACTIVE)
                .map(UserAccount::getId)
                .forEach(activeDoctorIds::add);
        Set<Long> incompatibleDoctorIds = new HashSet<>(
                sessionRepository.findDistinctDoctorIdsByStatusIn(INCOMPATIBLE_SESSION_STATUSES));
        Set<Long> heldDoctorIds = offerStore.heldDoctorIds(
                available.stream().map(DoctorCareProfile::getDoctorId).toList());

        return available.stream()
                .filter(profile -> activeDoctorIds.contains(profile.getDoctorId()))
                .filter(profile -> profile.getBusySessionId() == null)
                .filter(profile -> !incompatibleDoctorIds.contains(profile.getDoctorId()))
                .filter(profile -> !heldDoctorIds.contains(profile.getDoctorId()))
                .sorted(Comparator.comparing(DoctorCareProfile::getDoctorId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConsultationQueueEntry> previewNextWaitingQueueEntry() {
        return queueEntryRepository.findFirstByStatusOrderByQueueDateAscQueueNumberAsc(
                ConsultationQueueStatus.WAITING);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConsultationDispatchCandidate> inspectNextDispatchCandidate() {
        Optional<ConsultationQueueEntry> queueEntry = previewNextWaitingQueueEntry();
        if (queueEntry.isEmpty())
            return Optional.empty();
        return previewNextEligibleDoctor().map(doctor -> new ConsultationDispatchCandidate(
                queueEntry.get().getId(),
                queueEntry.get().getRequestId(),
                queueEntry.get().getMemberId(),
                queueEntry.get().getQueueDate(),
                queueEntry.get().getQueueNumber(),
                doctor.getDoctorId()));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public ConsultationDispatchState lockDispatchStateForOfferCommit() {
        return dispatchStateRepository.findSingletonForUpdate()
                .orElseThrow(() -> new AppException(ErrorCode.DATA_INTEGRITY_VIOLATION,
                        "Queue dispatch singleton is missing"));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<DoctorCareProfile> lockAndRevalidateEligibleDoctor(Long doctorId) {
        Optional<UserAccount> account = userAccountRepository.findByIdForUpdate(doctorId);
        if (account.isEmpty() || account.get().getRole() != UserRole.DOCTOR
                || account.get().getStatus() != AccountStatus.ACTIVE)
            return Optional.empty();
        return profileRepository.findByDoctorIdForUpdate(doctorId)
                .filter(profile -> profile.getDispatchStatus() == DoctorDispatchStatus.AVAILABLE)
                .filter(profile -> profile.getBusySessionId() == null)
                .filter(profile -> !sessionRepository.existsByDoctorIdAndStatusIn(
                        doctorId, INCOMPATIBLE_SESSION_STATUSES))
                .filter(profile -> !offerStore.heldDoctorIds(List.of(doctorId)).contains(doctorId));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void advanceLockedPointerAfterOfferCommit(ConsultationDispatchState lockedState, Long doctorId) {
        if (lockedState == null
                || !ConsultationDispatchState.SINGLETON_ID.equals(lockedState.getId())
                || doctorId == null)
            throw new AppException(ErrorCode.DATA_INTEGRITY_VIOLATION,
                    "Dispatch pointer requires the locked singleton and a committed offer doctor");
        lockedState.setLastDoctorId(doctorId);
        dispatchStateRepository.save(lockedState);
    }
}
