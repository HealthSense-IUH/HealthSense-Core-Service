package fit.iuh.se.hschat.service.session.impl;

import fit.iuh.se.hschat.dto.response.ConsultationSessionResponse;
import fit.iuh.se.hschat.entity.*;
import fit.iuh.se.hschat.entity.enums.*;
import fit.iuh.se.hschat.mapper.ConsultationMapper;
import fit.iuh.se.hschat.repository.*;
import fit.iuh.se.hschat.service.authorization.EpisodeHealthRecordAuthorizationService;
import fit.iuh.se.hschat.service.dispatch.DoctorDispatchSelectionService;
import fit.iuh.se.hschat.service.dispatch.event.DispatchRequested;
import fit.iuh.se.hschat.service.dispatch.offer.*;
import fit.iuh.se.hschat.service.session.QueueConsultationSessionService;
import fit.iuh.se.hsoperations.dto.command.NotificationIntent;
import fit.iuh.se.hsoperations.dto.command.OperationalEventCommand;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.event.OperationalEventPublisher;
import fit.iuh.se.hsbilling.entity.enums.CreditReservationStatus;
import fit.iuh.se.hsbilling.service.ConsultationCreditService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.*;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueConsultationSessionServiceImpl implements QueueConsultationSessionService {
    private final DoctorDispatchSelectionService dispatchSelection;
    private final UserAccountRepository userAccounts;
    private final ConsultationRequestRepository requests;
    private final ConsultationQueueEntryRepository queues;
    private final DoctorCareProfileRepository doctorProfiles;
    private final ConsultationSessionRepository sessions;
    private final ConsultationParticipantRepository participants;
    private final EpisodeHealthRecordAuthorizationService authorizations;
    private final DoctorOfferStore offers;
    private final ConsultationMapper mapper;
    private final OperationalEventPublisher operationalEvents;
    private final ApplicationEventPublisher applicationEvents;
    private final ConsultationCreditService consultationCredits;

    @NonFinal @Value("${app.consultation.dispatch.initial-session-minutes:15}") long initialSessionMinutes = 15;
    @NonFinal Clock clock = Clock.systemUTC();

    @Override
    @Transactional
    public ConsultationSessionResponse confirmMember(Long memberId, Long requestId, String offerId) {
        UserAccount member = userAccounts.findByIdForUpdate(memberId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
        requireAccount(member, UserRole.MEMBER);
        dispatchSelection.lockDispatchStateForOfferCommit();
        ConsultationRequest request = requests.findByIdForUpdate(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));
        if (!request.getMemberId().equals(memberId))
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);
        if (request.getFlowType() != ConsultationFlowType.QUEUE_DISPATCH_V1)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);

        ConsultationSession existing = sessions.findByRequestId(requestId).orElse(null);
        if (existing != null) return existingResult(existing, request, memberId);

        ConsultationQueueEntry queue = queues.findByRequestIdForUpdate(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.DATA_INTEGRITY_VIOLATION));
        if (!queue.getMemberId().equals(memberId)
                || request.getStatus() != ConsultationRequestStatus.QUEUED
                || queue.getStatus() != ConsultationQueueStatus.WAITING_MEMBER_CONFIRMATION)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);

        DoctorOffer offer = offers.findById(offerId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_OFFER_NOT_FOUND));
        requireOfferOwnership(offer, request, queue, memberId, offerId);
        if (offer.state() != DoctorOfferState.WAITING_MEMBER_CONFIRMATION
                && offer.state() != DoctorOfferState.CONFIRMED)
            throw new AppException(ErrorCode.CONSULTATION_OFFER_STALE);
        if (offer.doctorAcceptedAt() == null || offer.memberConfirmExpiresAt() == null)
            throw new AppException(ErrorCode.CONSULTATION_OFFER_STALE);

        UserAccount doctorAccount = userAccounts.findByIdForUpdate(offer.doctorId())
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));
        requireAccount(doctorAccount, UserRole.DOCTOR);
        DoctorCareProfile doctor = doctorProfiles.findByDoctorIdForUpdate(offer.doctorId())
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_CARE_PROFILE_NOT_FOUND));
        if (doctor.getDispatchStatus() != DoctorDispatchStatus.AVAILABLE || doctor.getBusySessionId() != null
                || sessions.existsByDoctorIdAndStatusIn(offer.doctorId(),
                        List.of(ConsultationStatus.SCHEDULED, ConsultationStatus.ACTIVE)))
            throw new AppException(ErrorCode.DOCTOR_NOT_ELIGIBLE_FOR_CONSULTATION);
        if (sessions.existsByMemberIdAndFlowTypeAndStatus(
                memberId, ConsultationFlowType.QUEUE_DISPATCH_V1, ConsultationStatus.ACTIVE))
            throw new AppException(ErrorCode.MEMBER_ALREADY_HAS_ACTIVE_CONSULTATION);

        Instant now = Instant.now(clock);
        DoctorOfferStore.ConfirmResult confirmed = offers.confirm(
                offerId, queue.getId(), memberId, offer.doctorId(), now);
        if (confirmed == DoctorOfferStore.ConfirmResult.EXPIRED)
            throw new AppException(ErrorCode.CONSULTATION_OFFER_EXPIRED);
        if (confirmed == DoctorOfferStore.ConfirmResult.STALE)
            throw new AppException(ErrorCode.CONSULTATION_OFFER_STALE);
        registerRedisCompletion(offer);

        Instant endsAt = now.plus(Duration.ofMinutes(initialSessionMinutes));
        ConsultationSession session = sessions.saveAndFlush(ConsultationSession.builder()
                .memberId(memberId).doctorId(offer.doctorId())
                .flowType(ConsultationFlowType.QUEUE_DISPATCH_V1)
                .creditPolicy(request.getCreditPolicy()).creditCost(request.getCreditCost())
                .sourceType(ConsultationSourceType.MEMBER_REQUEST)
                .status(ConsultationStatus.ACTIVE)
                .startedAt(now).activatedAt(now).blockStartedAt(now).endsAt(endsAt).supportEndsAt(endsAt)
                .continuationRound(0).requestId(requestId)
                .healthRecordId(firstSelectedRecord(request)).build());

        settleCredit(request, session);

        participants.save(ConsultationParticipant.builder().sessionId(session.getId()).userId(memberId)
                .role(ConsultationParticipantRole.MEMBER).joinedAt(now).active(true).build());
        participants.save(ConsultationParticipant.builder().sessionId(session.getId()).userId(offer.doctorId())
                .role(ConsultationParticipantRole.DOCTOR).joinedAt(now).active(true).build());
        authorizations.authorizeInitialRecords(session, request.getSelectedHealthRecordIds());

        doctor.setDispatchStatus(DoctorDispatchStatus.BUSY);
        doctor.setBusySessionId(session.getId());
        doctor.setDispatchStatusChangedAt(now);
        doctorProfiles.save(doctor);
        queue.setStatus(ConsultationQueueStatus.FULFILLED);
        queue.setFulfilledAt(now);
        queues.save(queue);
        request.setStatus(ConsultationRequestStatus.FULFILLED);
        request.setAssignedDoctorId(offer.doctorId());
        request.setConsultationSessionId(session.getId());
        requests.save(request);

        audit(session, offer, BusinessEventType.MEMBER_CONFIRMED, BusinessDomainType.REQUEST,
                requestId, memberId, List.of());
        audit(session, offer, BusinessEventType.DOCTOR_BUSY, BusinessDomainType.SESSION,
                session.getId(), memberId, List.of());
        audit(session, offer, BusinessEventType.SESSION_CREATED, BusinessDomainType.SESSION,
                session.getId(), memberId, activationNotifications(session));
        applicationEvents.publishEvent(new DispatchRequested("queue-session-created"));
        return response(session);
    }

    private ConsultationSessionResponse existingResult(
            ConsultationSession session, ConsultationRequest request, Long memberId) {
        if (session.getFlowType() != ConsultationFlowType.QUEUE_DISPATCH_V1
                || !session.getRequestId().equals(request.getId())
                || !session.getMemberId().equals(memberId)
                || !Objects.equals(request.getConsultationSessionId(), session.getId())
                || request.getStatus() != ConsultationRequestStatus.FULFILLED)
            throw new AppException(ErrorCode.CONSULTATION_OFFER_STALE);
        verifyCapturedCredit(request, session);
        cleanupStaleOfferAfterExistingSession(request, session);
        return response(session);
    }

    private void verifyCapturedCredit(ConsultationRequest request, ConsultationSession session) {
        if (request.getCreditPolicy() == ConsultationCreditPolicy.PER_SESSION_V1)
            consultationCredits.capture(request.getMemberId(), request.getId(), session.getId());
        else if (request.getCreditPolicy() == ConsultationCreditPolicy.PER_SESSION_CONFIRM_V2)
            consultationCredits.chargeSession(request.getMemberId(), request.getId(), session.getId(),
                    request.getCreditCost());
    }

    private void settleCredit(ConsultationRequest request, ConsultationSession session) {
        verifyCapturedCredit(request, session);
    }

    private ConsultationSessionResponse response(ConsultationSession session) {
        ConsultationSessionResponse response = mapper.toSessionResponse(session);
        if (session.getCreditPolicy() == ConsultationCreditPolicy.PER_SESSION_V1
                || session.getCreditPolicy() == ConsultationCreditPolicy.PER_SESSION_CONFIRM_V2)
            response.setCreditReservationStatus(CreditReservationStatus.CAPTURED);
        return response;
    }

    private void cleanupStaleOfferAfterExistingSession(ConsultationRequest request, ConsultationSession session) {
        try {
            queues.findByRequestId(request.getId()).flatMap(entry -> offers.findByQueueEntryId(entry.getId()))
                    .filter(offer -> offer.requestId().equals(request.getId())
                            && offer.doctorId().equals(session.getDoctorId()))
                    .ifPresent(offers::release);
        } catch (RuntimeException ex) {
            log.warn("Could not clean stale offer for committed session {}", session.getId(), ex);
        }
    }

    private void requireOfferOwnership(DoctorOffer offer, ConsultationRequest request,
            ConsultationQueueEntry queue, Long memberId, String offerId) {
        if (!offer.offerId().equals(offerId) || !offer.requestId().equals(request.getId())
                || !offer.queueEntryId().equals(queue.getId()) || !offer.memberId().equals(memberId))
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);
        DoctorOffer queueOffer = offers.findByQueueEntryId(queue.getId())
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_OFFER_STALE));
        DoctorOffer doctorOffer = offers.findByDoctorId(offer.doctorId())
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_OFFER_STALE));
        if (!queueOffer.offerId().equals(offerId) || !doctorOffer.offerId().equals(offerId))
            throw new AppException(ErrorCode.CONSULTATION_OFFER_STALE);
    }

    private void requireAccount(UserAccount account, UserRole role) {
        if (account.getRole() != role) throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);
        if (account.getStatus() != AccountStatus.ACTIVE) throw new AppException(ErrorCode.ACCOUNT_DISABLED);
    }

    private Long firstSelectedRecord(ConsultationRequest request) {
        return request.getSelectedHealthRecordIds() == null || request.getSelectedHealthRecordIds().isEmpty()
                ? null : request.getSelectedHealthRecordIds().getFirst();
    }

    private List<NotificationIntent> activationNotifications(ConsultationSession session) {
        String key = "queue-session:" + session.getId() + ":activated";
        return List.of(
                new NotificationIntent(session.getMemberId(), UserRole.MEMBER, NotificationType.CARE_ACTIVATED,
                        "Phiên tư vấn đã bắt đầu", "Phiên tư vấn của bạn đã sẵn sàng.",
                        BusinessDomainType.SESSION, session.getId(), key + ":member"),
                new NotificationIntent(session.getDoctorId(), UserRole.DOCTOR, NotificationType.CARE_ACTIVATED,
                        "Phiên tư vấn đã bắt đầu", "Phiên tư vấn đã được kích hoạt.",
                        BusinessDomainType.SESSION, session.getId(), key + ":doctor"));
    }

    private void audit(ConsultationSession session, DoctorOffer offer, BusinessEventType type,
            BusinessDomainType domain, Long domainId, Long actorId, List<NotificationIntent> notifications) {
        Map<String,String> metadata = new LinkedHashMap<>();
        metadata.put("offerId", offer.offerId());
        metadata.put("queueEntryId", offer.queueEntryId().toString());
        metadata.put("sessionId", session.getId().toString());
        metadata.put("startedAt", session.getStartedAt().toString());
        metadata.put("endsAt", session.getEndsAt().toString());
        operationalEvents.record(OperationalEventCommand.builder()
                .domainType(domain).domainId(domainId).eventType(type).actorType(BusinessActorType.USER)
                .actorUserId(actorId).actorRole(UserRole.MEMBER.name()).requestId(offer.requestId())
                .sessionId(session.getId()).memberId(session.getMemberId()).doctorId(session.getDoctorId())
                .newState(type == BusinessEventType.DOCTOR_BUSY ? DoctorDispatchStatus.BUSY.name()
                        : type == BusinessEventType.MEMBER_CONFIRMED ? ConsultationQueueStatus.FULFILLED.name()
                        : ConsultationStatus.ACTIVE.name())
                .metadata(metadata).idempotencyKey("queue-session:" + session.getId() + ":" + type)
                .occurredAt(session.getStartedAt()).notifications(notifications).build());
    }

    private void registerRedisCompletion(DoctorOffer offer) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) return;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                try {
                    if (status == STATUS_COMMITTED) offers.release(offer);
                    else offers.restoreMemberConfirmation(offer);
                } catch (RuntimeException ex) {
                    log.error("Could not finalize Redis offer {} after DB transaction status {}",
                            offer.offerId(), status, ex);
                }
            }
        });
    }
}
