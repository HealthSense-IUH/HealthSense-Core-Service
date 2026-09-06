package fit.iuh.se.hschat.service.dispatch.impl;

import fit.iuh.se.hschat.dto.response.DoctorConsultationOfferResponse;
import fit.iuh.se.hschat.dto.response.MinimalMemberIntakeContext;
import fit.iuh.se.hschat.entity.*;
import fit.iuh.se.hschat.entity.enums.*;
import fit.iuh.se.hschat.repository.*;
import fit.iuh.se.hschat.service.dispatch.DoctorDispatchSelectionService;
import fit.iuh.se.hschat.service.dispatch.DoctorOfferService;
import fit.iuh.se.hschat.service.dispatch.event.DispatchRequested;
import fit.iuh.se.hschat.service.dispatch.offer.*;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsoperations.dto.command.NotificationIntent;
import fit.iuh.se.hsoperations.dto.command.OperationalEventCommand;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.event.OperationalEventPublisher;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorOfferServiceImpl implements DoctorOfferService {
    private final DoctorDispatchSelectionService selectionService;
    private final DoctorOfferStore offerStore;
    private final ConsultationQueueEntryRepository queueRepository;
    private final ConsultationRequestRepository requestRepository;
    private final DoctorCareProfileRepository profileRepository;
    private final ConsultationSessionRepository sessionRepository;
    private final UserAccountRepository userAccountRepository;
    private final OperationalEventPublisher operationalEvents;
    private final ApplicationEventPublisher applicationEvents;

    @NonFinal @Value("${app.consultation.dispatch.doctor-offer-minutes:5}") long doctorOfferMinutes = 5;
    @NonFinal @Value("${app.consultation.dispatch.member-confirmation-minutes:15}") long memberConfirmationMinutes = 15;
    @NonFinal Clock clock = Clock.systemUTC();

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean dispatchOne() {
        ConsultationDispatchState state = selectionService.lockDispatchStateForOfferCommit();
        Optional<ConsultationQueueEntry> candidateEntry = queueRepository
                .findFirstByStatusForUpdate(ConsultationQueueStatus.WAITING);
        if (candidateEntry.isEmpty()) return false;

        Optional<DoctorCareProfile> candidateDoctor = selectionService.previewNextEligibleDoctor();
        if (candidateDoctor.isEmpty()) return false;
        ConsultationQueueEntry entry = candidateEntry.get();
        DoctorCareProfile doctor = selectionService.lockAndRevalidateEligibleDoctor(
                candidateDoctor.get().getDoctorId()).orElse(null);
        if (doctor == null) return false;

        Instant offeredAt = Instant.now(clock);
        DoctorOffer offer = new DoctorOffer(UUID.randomUUID().toString(), entry.getId(), entry.getRequestId(),
                entry.getMemberId(), doctor.getDoctorId(), DoctorOfferState.OFFERED_TO_DOCTOR,
                offeredAt, offeredAt.plus(Duration.ofMinutes(doctorOfferMinutes)), null, null);
        DoctorOfferStore.CreateResult created = offerStore.create(offer);
        if (created != DoctorOfferStore.CreateResult.CREATED) return false;
        cleanupRedisOnRollback(offer);

        if (entry.getStatus() != ConsultationQueueStatus.WAITING) {
            offerStore.release(offer);
            return false;
        }
        entry.setStatus(ConsultationQueueStatus.OFFERING_DOCTOR);
        queueRepository.save(entry);
        selectionService.advanceLockedPointerAfterOfferCommit(state, doctor.getDoctorId());
        auditOffer(offer, BusinessEventType.DOCTOR_OFFERED, null,
                ConsultationQueueStatus.WAITING, ConsultationQueueStatus.OFFERING_DOCTOR,
                List.of(new NotificationIntent(doctor.getDoctorId(), UserRole.DOCTOR,
                        NotificationType.DOCTOR_OFFER_AVAILABLE, "Lượt tư vấn mới",
                        "Có một lượt tư vấn mới. Vui lòng xác nhận trong 5 phút.",
                        BusinessDomainType.REQUEST, offer.requestId(), "offer:" + offer.offerId() + ":doctor")));
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorConsultationOfferResponse getCurrentOffer(Long doctorId) {
        return offerStore.findByDoctorId(doctorId).map(offer -> {
            if (!offer.doctorId().equals(doctorId)) throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);
            ConsultationSession session = sessionRepository.findByRequestId(offer.requestId()).orElse(null);
            if (session != null && session.getFlowType() == ConsultationFlowType.QUEUE_DISPATCH_V1
                    && session.getDoctorId().equals(doctorId)) {
                offerStore.release(offer);
                return null;
            }
            ConsultationRequest request = requestRepository.findById(offer.requestId())
                    .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_OFFER_STALE));
            return response(offer, request);
        }).orElse(null);
    }

    @Override
    @Transactional
    public DoctorConsultationOfferResponse accept(Long doctorId, String offerId) {
        selectionService.lockDispatchStateForOfferCommit();
        DoctorOffer offer = ownedOffer(doctorId, offerId);
        ConsultationQueueEntry entry = queueRepository.findByIdForUpdate(offer.queueEntryId())
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_OFFER_STALE));
        ConsultationRequest request = requestRepository.findByIdForUpdate(offer.requestId())
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_OFFER_STALE));
        var account = userAccountRepository.findByIdForUpdate(doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));
        DoctorCareProfile doctor = profileRepository.findByDoctorIdForUpdate(doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_CARE_PROFILE_NOT_FOUND));
        if (account.getRole() != UserRole.DOCTOR || account.getStatus() != AccountStatus.ACTIVE
                || doctor.getDispatchStatus() != DoctorDispatchStatus.AVAILABLE
                || doctor.getBusySessionId() != null
                || sessionRepository.existsByDoctorIdAndStatusIn(doctorId,
                        List.of(ConsultationStatus.SCHEDULED, ConsultationStatus.ACTIVE)))
            throw new AppException(ErrorCode.DOCTOR_NOT_ELIGIBLE_FOR_CONSULTATION);

        if (offer.state() == DoctorOfferState.WAITING_MEMBER_CONFIRMATION) {
            if (entry.getStatus() != ConsultationQueueStatus.WAITING_MEMBER_CONFIRMATION)
                throw new AppException(ErrorCode.CONSULTATION_OFFER_STALE);
            return response(offer, request);
        }
        if (entry.getStatus() != ConsultationQueueStatus.OFFERING_DOCTOR
                || request.getStatus() != ConsultationRequestStatus.QUEUED
                || doctor.getDispatchStatus() != DoctorDispatchStatus.AVAILABLE)
            throw new AppException(ErrorCode.CONSULTATION_OFFER_STALE);

        Instant now = Instant.now(clock);
        Instant memberDeadline = now.plus(Duration.ofMinutes(memberConfirmationMinutes));
        DoctorOfferStore.AcceptResult result = offerStore.accept(offerId, doctorId, now, memberDeadline);
        if (result == DoctorOfferStore.AcceptResult.EXPIRED) throw new AppException(ErrorCode.CONSULTATION_OFFER_EXPIRED);
        if (result == DoctorOfferStore.AcceptResult.STALE) throw new AppException(ErrorCode.CONSULTATION_OFFER_STALE);
        DoctorOffer accepted = offerStore.findById(offerId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_OFFER_STALE));
        if (result == DoctorOfferStore.AcceptResult.ACCEPTED) {
            cleanupRedisOnRollback(accepted);
            entry.setStatus(ConsultationQueueStatus.WAITING_MEMBER_CONFIRMATION);
            queueRepository.save(entry);
            auditOffer(accepted, BusinessEventType.DOCTOR_OFFER_ACCEPTED, doctorId,
                    ConsultationQueueStatus.OFFERING_DOCTOR, ConsultationQueueStatus.WAITING_MEMBER_CONFIRMATION,
                    List.of());
            auditOffer(accepted, BusinessEventType.MEMBER_CONFIRMATION_STARTED, doctorId,
                    ConsultationQueueStatus.OFFERING_DOCTOR, ConsultationQueueStatus.WAITING_MEMBER_CONFIRMATION,
                    List.of(new NotificationIntent(accepted.memberId(), UserRole.MEMBER,
                            NotificationType.MEMBER_CONFIRMATION_REQUIRED, "Bác sĩ đã sẵn sàng tư vấn",
                            "Bác sĩ đã sẵn sàng tư vấn. Vui lòng xác nhận tham gia trong 15 phút.",
                            BusinessDomainType.REQUEST, accepted.requestId(),
                            "offer:" + accepted.offerId() + ":member-confirmation")));
        }
        return response(accepted, request);
    }

    @Override
    @Transactional
    public DoctorConsultationOfferResponse reject(Long doctorId, String offerId) {
        selectionService.lockDispatchStateForOfferCommit();
        DoctorOffer offer = ownedOffer(doctorId, offerId);
        if (offer.state() == DoctorOfferState.WAITING_MEMBER_CONFIRMATION)
            throw new AppException(ErrorCode.CONSULTATION_OFFER_ALREADY_ACCEPTED);
        ConsultationQueueEntry entry = queueRepository.findByIdForUpdate(offer.queueEntryId())
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_OFFER_STALE));
        ConsultationRequest request = requestRepository.findByIdForUpdate(offer.requestId())
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_OFFER_STALE));
        DoctorCareProfile doctor = profileRepository.findByDoctorIdForUpdate(doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_CARE_PROFILE_NOT_FOUND));
        if (entry.getStatus() != ConsultationQueueStatus.OFFERING_DOCTOR
                || !offerStore.release(offer)) throw new AppException(ErrorCode.CONSULTATION_OFFER_STALE);
        entry.setStatus(ConsultationQueueStatus.WAITING);
        doctor.setDispatchStatus(DoctorDispatchStatus.UNAVAILABLE);
        doctor.setDispatchStatusChangedAt(Instant.now(clock));
        queueRepository.save(entry); profileRepository.save(doctor);
        auditOffer(offer, BusinessEventType.DOCTOR_OFFER_REJECTED, doctorId,
                ConsultationQueueStatus.OFFERING_DOCTOR, ConsultationQueueStatus.WAITING, List.of());
        applicationEvents.publishEvent(new DispatchRequested("doctor-rejected"));
        return response(withState(offer, DoctorOfferState.DOCTOR_REJECTED), request);
    }

    @Override @Transactional
    public void processDoctorTimeout(String offerId) {
        selectionService.lockDispatchStateForOfferCommit();
        DoctorOffer offer = offerStore.findById(offerId).orElse(null);
        if (offer == null || offer.state() != DoctorOfferState.OFFERED_TO_DOCTOR
                || Instant.now(clock).isBefore(offer.doctorOfferExpiresAt())) return;
        ConsultationQueueEntry entry = queueRepository.findByIdForUpdate(offer.queueEntryId()).orElse(null);
        DoctorCareProfile doctor = profileRepository.findByDoctorIdForUpdate(offer.doctorId()).orElse(null);
        if (entry == null || doctor == null || entry.getStatus() != ConsultationQueueStatus.OFFERING_DOCTOR) return;
        if (!offerStore.release(offer)) return;
        entry.setStatus(ConsultationQueueStatus.WAITING);
        doctor.setDispatchStatus(DoctorDispatchStatus.UNAVAILABLE);
        doctor.setDispatchStatusChangedAt(Instant.now(clock));
        queueRepository.save(entry); profileRepository.save(doctor);
        auditOffer(offer, BusinessEventType.DOCTOR_OFFER_TIMEOUT, null,
                ConsultationQueueStatus.OFFERING_DOCTOR, ConsultationQueueStatus.WAITING, List.of());
        applicationEvents.publishEvent(new DispatchRequested("doctor-timeout"));
    }

    @Override @Transactional
    public void processMemberConfirmationTimeout(String offerId) {
        selectionService.lockDispatchStateForOfferCommit();
        DoctorOffer offer = offerStore.findById(offerId).orElse(null);
        if (offer == null || offer.memberConfirmExpiresAt() == null
                || Instant.now(clock).isBefore(offer.memberConfirmExpiresAt())) return;
        if (offer.state() == DoctorOfferState.CONFIRMED) {
            if (sessionRepository.findByRequestId(offer.requestId()).isPresent()) {
                offerStore.release(offer);
                return;
            }
            if (!offerStore.restoreMemberConfirmation(offer)) return;
            offer = withState(offer, DoctorOfferState.WAITING_MEMBER_CONFIRMATION);
        }
        if (offer.state() != DoctorOfferState.WAITING_MEMBER_CONFIRMATION) return;
        ConsultationQueueEntry entry = queueRepository.findByIdForUpdate(offer.queueEntryId()).orElse(null);
        ConsultationRequest request = requestRepository.findByIdForUpdate(offer.requestId()).orElse(null);
        DoctorCareProfile doctor = profileRepository.findByDoctorIdForUpdate(offer.doctorId()).orElse(null);
        if (entry == null || request == null || doctor == null
                || entry.getStatus() != ConsultationQueueStatus.WAITING_MEMBER_CONFIRMATION
                || request.getStatus() != ConsultationRequestStatus.QUEUED) return;
        if (!offerStore.release(offer)) return;
        Instant now = Instant.now(clock);
        entry.setStatus(ConsultationQueueStatus.TIMED_OUT); entry.setTimedOutAt(now);
        request.setStatus(ConsultationRequestStatus.TIMED_OUT); request.setExpiredAt(now);
        doctor.setDispatchStatus(DoctorDispatchStatus.AVAILABLE); doctor.setDispatchStatusChangedAt(now);
        queueRepository.save(entry); requestRepository.save(request); profileRepository.save(doctor);
        auditOffer(offer, BusinessEventType.MEMBER_CONFIRMATION_TIMEOUT, null,
                ConsultationQueueStatus.WAITING_MEMBER_CONFIRMATION, ConsultationQueueStatus.TIMED_OUT,
                List.of(new NotificationIntent(offer.memberId(), UserRole.MEMBER,
                        NotificationType.MEMBER_CONFIRMATION_EXPIRED, "Lượt tư vấn đã hết hạn",
                        "Thời gian xác nhận tham gia tư vấn đã hết. Vui lòng tạo yêu cầu mới khi cần.",
                        BusinessDomainType.REQUEST, offer.requestId(), "offer:" + offer.offerId() + ":expired")));
        applicationEvents.publishEvent(new DispatchRequested("member-confirmation-timeout"));
    }

    @Override @Transactional
    public void reconcileMissingOffers() {
        selectionService.lockDispatchStateForOfferCommit();
        List<ConsultationQueueEntry> unresolved = queueRepository.findByStatusIn(List.of(
                ConsultationQueueStatus.OFFERING_DOCTOR, ConsultationQueueStatus.WAITING_MEMBER_CONFIRMATION));
        for (ConsultationQueueEntry entry : unresolved) {
            Optional<DoctorOffer> current = offerStore.findByQueueEntryId(entry.getId());
            if (entry.getStatus() == ConsultationQueueStatus.WAITING_MEMBER_CONFIRMATION
                    && current.filter(offer -> offer.state() == DoctorOfferState.CONFIRMED).isPresent()
                    && sessionRepository.findByRequestId(entry.getRequestId()).isEmpty()) {
                offerStore.restoreMemberConfirmation(current.get());
                continue;
            }
            if (current.isEmpty()) {
                ConsultationQueueStatus previous = entry.getStatus();
                entry.setStatus(ConsultationQueueStatus.WAITING);
                queueRepository.save(entry);
                log.warn("Recovered queue entry {} from {} because its Redis offer was missing",
                        entry.getId(), previous);
            }
        }
    }

    private DoctorOffer ownedOffer(Long doctorId, String offerId) {
        DoctorOffer offer = offerStore.findById(offerId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_OFFER_NOT_FOUND));
        if (!offer.doctorId().equals(doctorId)) throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);
        DoctorOffer current = offerStore.findByDoctorId(doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_OFFER_STALE));
        if (!current.offerId().equals(offerId)) throw new AppException(ErrorCode.CONSULTATION_OFFER_STALE);
        return offer;
    }

    private DoctorConsultationOfferResponse response(DoctorOffer offer, ConsultationRequest request) {
        return new DoctorConsultationOfferResponse(offer.offerId(), offer.queueEntryId(), offer.requestId(),
                offer.state(), offer.offeredAt(), offer.doctorOfferExpiresAt(), offer.doctorAcceptedAt(),
                offer.memberConfirmExpiresAt(), new MinimalMemberIntakeContext(request.getReasonForCare(),
                request.getCurrentConcern(), request.getCareGoal(), request.getMemberNote(),
                request.getRelevantSelfReportedContext()));
    }

    private DoctorOffer withState(DoctorOffer offer, DoctorOfferState state) {
        return new DoctorOffer(offer.offerId(), offer.queueEntryId(), offer.requestId(), offer.memberId(),
                offer.doctorId(), state, offer.offeredAt(), offer.doctorOfferExpiresAt(),
                offer.doctorAcceptedAt(), offer.memberConfirmExpiresAt());
    }

    private void cleanupRedisOnRollback(DoctorOffer offer) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    try { offerStore.release(offer); }
                    catch (RuntimeException ex) { log.error("Could not clean rolled-back offer {}", offer.offerId(), ex); }
                }
            }
        });
    }

    private void auditOffer(DoctorOffer offer, BusinessEventType type, Long actorId,
            ConsultationQueueStatus previous, ConsultationQueueStatus next, List<NotificationIntent> notifications) {
        Map<String,String> metadata = new LinkedHashMap<>();
        metadata.put("offerId", offer.offerId());
        metadata.put("queueEntryId", offer.queueEntryId().toString());
        metadata.put("doctorOfferExpiresAt", offer.doctorOfferExpiresAt().toString());
        if (offer.memberConfirmExpiresAt() != null)
            metadata.put("memberConfirmExpiresAt", offer.memberConfirmExpiresAt().toString());
        operationalEvents.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.REQUEST).domainId(offer.requestId()).eventType(type)
                .actorType(actorId == null ? BusinessActorType.SYSTEM : BusinessActorType.USER)
                .actorUserId(actorId).actorRole(actorId == null ? null : UserRole.DOCTOR.name())
                .requestId(offer.requestId()).memberId(offer.memberId()).doctorId(offer.doctorId())
                .previousState(previous.name()).newState(next.name()).metadata(metadata)
                .idempotencyKey("offer:" + offer.offerId() + ":" + type).occurredAt(Instant.now(clock))
                .notifications(notifications).build());
    }
}
