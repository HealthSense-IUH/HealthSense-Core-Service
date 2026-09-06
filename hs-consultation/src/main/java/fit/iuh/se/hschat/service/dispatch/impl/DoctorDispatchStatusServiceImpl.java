package fit.iuh.se.hschat.service.dispatch.impl;

import fit.iuh.se.hschat.dto.request.UpdateDoctorDispatchPreferencesRequest;
import fit.iuh.se.hschat.dto.request.UpdateDoctorDispatchStatusRequest;
import fit.iuh.se.hschat.dto.response.DoctorDispatchStatusResponse;
import fit.iuh.se.hschat.entity.DoctorCareProfile;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.entity.enums.DoctorDispatchStatus;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.repository.DoctorCareProfileRepository;
import fit.iuh.se.hschat.service.dispatch.DoctorDispatchStatusService;
import fit.iuh.se.hschat.service.dispatch.DoctorDispatchSelectionService;
import fit.iuh.se.hschat.service.dispatch.DoctorOfferService;
import fit.iuh.se.hschat.service.dispatch.offer.DoctorOfferState;
import fit.iuh.se.hschat.service.dispatch.offer.DoctorOfferStore;
import fit.iuh.se.hschat.service.dispatch.event.DoctorDispatchStatusChanged;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsoperations.dto.command.OperationalEventCommand;
import fit.iuh.se.hsoperations.entity.enums.BusinessActorType;
import fit.iuh.se.hsoperations.entity.enums.BusinessDomainType;
import fit.iuh.se.hsoperations.entity.enums.BusinessEventType;
import fit.iuh.se.hsoperations.event.OperationalEventPublisher;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DoctorDispatchStatusServiceImpl implements DoctorDispatchStatusService {

    static final List<ConsultationStatus> INCOMPATIBLE_SESSION_STATUSES = List.of(
            ConsultationStatus.SCHEDULED,
            ConsultationStatus.ACTIVE);

    DoctorCareProfileRepository profileRepository;
    ConsultationSessionRepository sessionRepository;
    UserAccountRepository userAccountRepository;
    OperationalEventPublisher operationalEventPublisher;
    ApplicationEventPublisher applicationEventPublisher;
    DoctorDispatchSelectionService selectionService;
    DoctorOfferStore offerStore;
    DoctorOfferService offerService;

    @NonFinal
    Clock clock = Clock.systemUTC();

    @Override
    @Transactional(readOnly = true)
    public DoctorDispatchStatusResponse getStatus(Long doctorId) {
        validateActiveDoctor(userAccountRepository.findById(doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND)));
        return toResponse(profileRepository.findByDoctorId(doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_CARE_PROFILE_NOT_FOUND)));
    }

    @Override
    @Transactional
    public DoctorDispatchStatusResponse updateStatus(Long doctorId, UpdateDoctorDispatchStatusRequest request) {
        selectionService.lockDispatchStateForOfferCommit();
        validateActiveDoctor(userAccountRepository.findByIdForUpdate(doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND)));
        DoctorCareProfile profile = profileRepository.findByDoctorIdForUpdate(doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_CARE_PROFILE_NOT_FOUND));
        DoctorDispatchStatus current = profile.getDispatchStatus();
        DoctorDispatchStatus target = request.dispatchStatus();

        if (target == DoctorDispatchStatus.BUSY
                || current == DoctorDispatchStatus.BUSY
                || current == target
                || !isManualTransition(current, target))
            throw new AppException(ErrorCode.INVALID_DOCTOR_DISPATCH_STATUS);

        var activeOffer = offerStore.findByDoctorId(doctorId);
        if (target == DoctorDispatchStatus.UNAVAILABLE && activeOffer.isPresent()) {
            if (activeOffer.get().state() == DoctorOfferState.WAITING_MEMBER_CONFIRMATION)
                throw new AppException(ErrorCode.CONSULTATION_OFFER_ALREADY_ACCEPTED);
            offerService.reject(doctorId, activeOffer.get().offerId());
            Instant changedAt = profile.getDispatchStatusChangedAt();
            auditStatusChange(profile, doctorId, current, target, changedAt);
            applicationEventPublisher.publishEvent(new DoctorDispatchStatusChanged(
                    doctorId, current, target, changedAt));
            return toResponse(profile);
        }

        if (target == DoctorDispatchStatus.AVAILABLE) {
            if (profile.getBusySessionId() != null
                    || sessionRepository.existsByDoctorIdAndStatusIn(doctorId, INCOMPATIBLE_SESSION_STATUSES))
                throw new AppException(ErrorCode.DOCTOR_NOT_ELIGIBLE_FOR_CONSULTATION,
                        "Doctor has an incompatible active or scheduled consultation");
        }

        Instant changedAt = Instant.now(clock);
        profile.setDispatchStatus(target);
        profile.setDispatchStatusChangedAt(changedAt);
        profile = profileRepository.save(profile);
        auditStatusChange(profile, doctorId, current, target, changedAt);
        applicationEventPublisher.publishEvent(new DoctorDispatchStatusChanged(
                doctorId, current, target, changedAt));
        return toResponse(profile);
    }

    @Override
    @Transactional
    public DoctorDispatchStatusResponse updatePreferences(
            Long doctorId, UpdateDoctorDispatchPreferencesRequest request) {
        validateActiveDoctor(userAccountRepository.findByIdForUpdate(doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND)));
        DoctorCareProfile profile = profileRepository.findByDoctorIdForUpdate(doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_CARE_PROFILE_NOT_FOUND));
        boolean previous = Boolean.TRUE.equals(profile.getStopAfterCurrentSession());
        boolean next = request.stopAfterCurrentSession();
        if (previous == next)
            return toResponse(profile);

        profile.setStopAfterCurrentSession(next);
        profile = profileRepository.save(profile);
        operationalEventPublisher.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.ACCOUNT)
                .domainId(doctorId)
                .eventType(BusinessEventType.DOCTOR_STOP_AFTER_CURRENT_CHANGED)
                .actorType(BusinessActorType.USER)
                .actorUserId(doctorId)
                .actorRole(UserRole.DOCTOR.name())
                .doctorId(doctorId)
                .previousState(Boolean.toString(previous))
                .newState(Boolean.toString(next))
                .metadata(Map.of("dispatchStatus", profile.getDispatchStatus().name()))
                .idempotencyKey("doctor:" + doctorId + ":stop-after-current:" + next + ":" + profile.getVersion())
                .occurredAt(Instant.now(clock))
                .notifications(List.of())
                .build());
        return toResponse(profile);
    }

    private boolean isManualTransition(DoctorDispatchStatus current, DoctorDispatchStatus target) {
        return current == DoctorDispatchStatus.UNAVAILABLE && target == DoctorDispatchStatus.AVAILABLE
                || current == DoctorDispatchStatus.AVAILABLE && target == DoctorDispatchStatus.UNAVAILABLE;
    }

    private void validateActiveDoctor(UserAccount account) {
        if (account.getRole() != UserRole.DOCTOR || account.getStatus() != AccountStatus.ACTIVE)
            throw new AppException(ErrorCode.DOCTOR_NOT_FOUND);
    }

    private DoctorDispatchStatusResponse toResponse(DoctorCareProfile profile) {
        boolean effectivelyDispatchable = false;
        if (profile.getDispatchStatus() == DoctorDispatchStatus.AVAILABLE) {
            try {
                effectivelyDispatchable = profile.getBusySessionId() == null
                        && !sessionRepository.existsByDoctorIdAndStatusIn(
                                profile.getDoctorId(), INCOMPATIBLE_SESSION_STATUSES)
                        && offerStore.heldDoctorIds(List.of(profile.getDoctorId())).isEmpty();
            }
            catch (AppException ex) {
                if (ex.getErrorCode() != ErrorCode.DISPATCH_TEMPORARILY_UNAVAILABLE) throw ex;
            }
        }
        return new DoctorDispatchStatusResponse(
                profile.getDoctorId(),
                profile.getDispatchStatus(),
                effectivelyDispatchable,
                Boolean.TRUE.equals(profile.getStopAfterCurrentSession()),
                profile.getBusySessionId(),
                profile.getDispatchStatusChangedAt());
    }

    private void auditStatusChange(DoctorCareProfile profile, Long doctorId,
            DoctorDispatchStatus previous, DoctorDispatchStatus next, Instant changedAt) {
        operationalEventPublisher.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.ACCOUNT)
                .domainId(doctorId)
                .eventType(next == DoctorDispatchStatus.AVAILABLE
                        ? BusinessEventType.DOCTOR_AVAILABLE : BusinessEventType.DOCTOR_UNAVAILABLE)
                .actorType(BusinessActorType.USER)
                .actorUserId(doctorId)
                .actorRole(UserRole.DOCTOR.name())
                .doctorId(doctorId)
                .previousState(previous.name())
                .newState(next.name())
                .idempotencyKey("doctor:" + doctorId + ":dispatch-status:" + next + ":" + profile.getVersion())
                .occurredAt(changedAt)
                .notifications(List.of())
                .build());
    }
}
