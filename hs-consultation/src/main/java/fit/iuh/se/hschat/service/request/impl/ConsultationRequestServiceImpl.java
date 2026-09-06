package fit.iuh.se.hschat.service.request.impl;

import fit.iuh.se.hschat.dto.request.*;
import fit.iuh.se.hschat.dto.response.*;
import fit.iuh.se.hschat.entity.CareServicePackage;
import fit.iuh.se.hschat.entity.ConsultationMoreInfoCycle;
import fit.iuh.se.hschat.entity.ConsultationRequest;
import fit.iuh.se.hschat.entity.ConsultationQueueCounter;
import fit.iuh.se.hschat.entity.ConsultationQueueEntry;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.ConsultationDispatchState;
import fit.iuh.se.hschat.entity.DoctorCareProfile;
import fit.iuh.se.hschat.entity.enums.*;
import fit.iuh.se.hschat.mapper.ConsultationMapper;
import fit.iuh.se.hschat.repository.CareServicePackageRepository;
import fit.iuh.se.hschat.repository.ConsultationMoreInfoCycleRepository;
import fit.iuh.se.hschat.repository.ConsultationRequestRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.repository.DoctorCareProfileRepository;
import fit.iuh.se.hschat.repository.ConsultationQueueCounterRepository;
import fit.iuh.se.hschat.repository.ConsultationQueueEntryRepository;
import fit.iuh.se.hschat.repository.ConsultationDispatchStateRepository;
import fit.iuh.se.hschat.service.request.ConsultationRequestService;
import fit.iuh.se.hschat.service.ConsultationFlowGuard;
import fit.iuh.se.hschat.service.dispatch.DoctorDispatchSelectionService;
import fit.iuh.se.hschat.service.dispatch.event.DispatchRequested;
import fit.iuh.se.hschat.service.dispatch.offer.DoctorOffer;
import fit.iuh.se.hschat.service.dispatch.offer.DoctorOfferState;
import fit.iuh.se.hschat.service.dispatch.offer.DoctorOfferStore;
import fit.iuh.se.hschat.service.reservation.DoctorReservationService;
import fit.iuh.se.hschat.service.agreement.CareServiceAgreementService;
import fit.iuh.se.hschat.service.payment.PaymentCancellationService;
import fit.iuh.se.hshealthrecord.entity.HealthRecord;
import fit.iuh.se.hshealthrecord.repository.HealthRecordRepository;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.UserProfile;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import fit.iuh.se.hsoperations.dto.command.NotificationIntent;
import fit.iuh.se.hsoperations.dto.command.OperationalEventCommand;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.event.OperationalEventPublisher;
import jakarta.persistence.criteria.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConsultationRequestServiceImpl implements ConsultationRequestService {

    static final List<ConsultationRequestStatus> UNRESOLVED_REQUEST_STATUSES = List.of(
            ConsultationRequestStatus.PENDING_REVIEW,
            ConsultationRequestStatus.NEED_MORE_INFO,
            ConsultationRequestStatus.WAITING_ACCEPTANCE,
            ConsultationRequestStatus.WAITING_PAYMENT
    );
    static final List<ConsultationRequestStatus> ACTIVE_RESERVATION_STATUSES = List.of(
            ConsultationRequestStatus.WAITING_ACCEPTANCE,
            ConsultationRequestStatus.WAITING_PAYMENT
    );
    static final List<ConsultationStatus> MEMBER_BUSY_SESSION_STATUSES = List.of(
            ConsultationStatus.SCHEDULED,
            ConsultationStatus.ACTIVE
    );
    static final List<ConsultationQueueStatus> UNRESOLVED_QUEUE_STATUSES = List.of(
            ConsultationQueueStatus.WAITING,
            ConsultationQueueStatus.OFFERING_DOCTOR,
            ConsultationQueueStatus.WAITING_MEMBER_CONFIRMATION
    );

    ConsultationRequestRepository requestRepository;
    ConsultationMoreInfoCycleRepository moreInfoCycleRepository;
    ConsultationSessionRepository sessionRepository;
    HealthRecordRepository healthRecordRepository;
    UserAccountRepository userAccountRepository;
    CareServicePackageRepository packageRepository;
    DoctorCareProfileRepository doctorCareProfileRepository;
    ConsultationQueueEntryRepository queueEntryRepository;
    ConsultationQueueCounterRepository queueCounterRepository;
    ConsultationDispatchStateRepository dispatchStateRepository;
    DoctorReservationService reservationService;
    CareServiceAgreementService agreementService;
    PaymentCancellationService paymentCancellationService;
    ConsultationMapper mapper;
    OperationalEventPublisher OperationalEventPublisher;
    DoctorOfferStore doctorOfferStore;
    DoctorDispatchSelectionService doctorDispatchSelectionService;
    ApplicationEventPublisher applicationEventPublisher;

    @NonFinal
    @Value("${app.consultation.payment-deadline-minutes:30}")
    long paymentDeadlineMinutes;

    @NonFinal
    @Value("${app.consultation.business-timezone:Asia/Ho_Chi_Minh}")
    String businessTimezone;

    @NonFinal
    Clock clock = Clock.systemUTC();

    @Override
    @Transactional
    public ConsultationRequestResponse createRequest(Long memberId, CreateConsultationRequest request) {
        log.info("Creating consultation request for member {}", memberId);

        UserAccount member = userAccountRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
        if (member.getRole() != UserRole.MEMBER)
            throw new AppException(ErrorCode.MEMBER_NOT_FOUND);

        if (sessionRepository.existsByMemberIdAndStatusIn(memberId, MEMBER_BUSY_SESSION_STATUSES))
            throw new AppException(ErrorCode.MEMBER_ALREADY_HAS_ACTIVE_CONSULTATION);

        if (queueEntryRepository.existsByMemberIdAndStatusIn(memberId, UNRESOLVED_QUEUE_STATUSES)
                || requestRepository.existsByMemberIdAndStatusIn(memberId, UNRESOLVED_REQUEST_STATUSES))
            throw new AppException(ErrorCode.MEMBER_ALREADY_HAS_PENDING_CONSULTATION_REQUEST);

        List<Long> selectedHealthRecordIds = normalizeHealthRecordIds(
                request.getSelectedHealthRecordIds(),
                request.getHealthRecordId()
        );
        validateHealthRecordOwners(selectedHealthRecordIds, memberId);

        Instant now = Instant.now(clock);
        LocalDate queueDate = now.atZone(ZoneId.of(businessTimezone)).toLocalDate();

        // This singleton lock serializes counter-row creation as well as increments. It makes
        // the first admission of a business date safe without relying on COUNT + 1.
        dispatchStateRepository.findSingletonForUpdate()
                .orElseThrow(() -> new AppException(ErrorCode.DATA_INTEGRITY_VIOLATION,
                        "Queue dispatch singleton is missing"));
        ConsultationQueueCounter counter = queueCounterRepository.findByQueueDateForUpdate(queueDate)
                .orElseGet(() -> ConsultationQueueCounter.builder()
                        .queueDate(queueDate)
                        .lastNumber(0L)
                        .build());
        long queueNumber = counter.getLastNumber() + 1;
        counter.setLastNumber(queueNumber);
        queueCounterRepository.save(counter);

        ConsultationRequest consultationRequest = ConsultationRequest.builder()
                .memberId(memberId)
                .flowType(ConsultationFlowType.QUEUE_DISPATCH_V1)
                .healthRecordId(selectedHealthRecordIds.isEmpty() ? null : selectedHealthRecordIds.getFirst())
                .reason(request.getReasonForCare())
                .reasonForCare(request.getReasonForCare())
                .currentConcern(request.getCurrentConcern())
                .careGoal(request.getCareGoal())
                .memberNote(request.getMemberNote())
                .relevantSelfReportedContext(request.getRelevantSelfReportedContext())
                .selectedHealthRecordIds(selectedHealthRecordIds)
                .status(ConsultationRequestStatus.QUEUED)
                .build();

        consultationRequest = requestRepository.saveAndFlush(consultationRequest);
        ConsultationQueueEntry queueEntry = queueEntryRepository.save(ConsultationQueueEntry.builder()
                .requestId(consultationRequest.getId())
                .memberId(memberId)
                .queueDate(queueDate)
                .queueNumber(queueNumber)
                .status(ConsultationQueueStatus.WAITING)
                .queuedAt(now)
                .build());
        auditQueueAdmission(consultationRequest, queueEntry, memberId);
        applicationEventPublisher.publishEvent(new DispatchRequested("queue-admitted"));
        return toRequestResponse(consultationRequest);
    }

    @Override
    @Transactional
    public ConsultationRequestResponse approveRequest(Long actorId, UserRole actorRole, Long requestId, ApproveConsultationRequest request) {
        if (actorRole != UserRole.CARE_COORDINATOR)
            throw new AppException(ErrorCode.ACCESS_DENIED, "Only a Care Coordinator may reserve a doctor");
        log.info("Reserving doctor for consultation request {} by actor {} with role {}", requestId, actorId, actorRole);

        ConsultationRequest consultationRequest = requestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));
        ConsultationFlowGuard.requireLegacy(consultationRequest);

        if (consultationRequest.getStatus() != ConsultationRequestStatus.PENDING_REVIEW)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);

        Long memberId = consultationRequest.getMemberId();
        validateMember(memberId);

        if (sessionRepository.existsByMemberIdAndStatusIn(memberId, MEMBER_BUSY_SESSION_STATUSES))
            throw new AppException(ErrorCode.MEMBER_ALREADY_HAS_ACTIVE_CONSULTATION);

        Instant now = Instant.now();
        var reservation = reservationService.reserve(
                consultationRequest,
                actorId,
                request.getDoctorId(),
                now.plus(paymentDeadlineMinutes, ChronoUnit.MINUTES)
        );
        consultationRequest.setStatus(ConsultationRequestStatus.WAITING_ACCEPTANCE);
        consultationRequest.setAssignedDoctorId(request.getDoctorId());
        consultationRequest.setDoctorReservedAt(reservation.getReservedAt());
        consultationRequest.setPaymentDeadline(reservation.getExpiresAt());
        consultationRequest.setReviewedByAdminId(actorId);
        consultationRequest.setReviewedAt(now);
        consultationRequest.setMoreInfoReason(null);
        consultationRequest.setIntakeFrozenAt(now);
        consultationRequest = requestRepository.save(consultationRequest);
        agreementService.createForReservation(consultationRequest);
        auditRequest(consultationRequest, BusinessEventType.DOCTOR_RESERVED, actorId, actorRole,
                ConsultationRequestStatus.PENDING_REVIEW, ConsultationRequestStatus.WAITING_ACCEPTANCE,
                null, null);
        return toRequestResponse(consultationRequest);
    }

    @Override
    @Transactional
    public ConsultationRequestResponse rejectRequest(Long actorId, UserRole actorRole, Long requestId, RejectConsultationRequest request) {
        validateConsultationManager(actorRole);
        ConsultationRequest consultationRequest = requestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));
        ConsultationFlowGuard.requireLegacy(consultationRequest);

        if (consultationRequest.getStatus() != ConsultationRequestStatus.PENDING_REVIEW
                && consultationRequest.getStatus() != ConsultationRequestStatus.NEED_MORE_INFO
                && consultationRequest.getStatus() != ConsultationRequestStatus.WAITING_ACCEPTANCE
                && consultationRequest.getStatus() != ConsultationRequestStatus.WAITING_PAYMENT)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);

        reservationService.release(consultationRequest, DoctorReservationReleaseReason.COORDINATOR_REJECTED);
        agreementService.invalidateCurrent(requestId, "Request rejected before care activation");

        Instant now = Instant.now();
        consultationRequest.setStatus(ConsultationRequestStatus.REJECTED);
        consultationRequest.setReviewedByAdminId(actorId);
        consultationRequest.setReviewedAt(now);
        consultationRequest.setRejectionReason(request.getRejectionReason());

        consultationRequest = requestRepository.save(consultationRequest);
        auditRequest(consultationRequest, BusinessEventType.REQUEST_REJECTED, actorId, actorRole,
                null, ConsultationRequestStatus.REJECTED, request.getRejectionReason(), NotificationType.REQUEST_REJECTED);
        return toRequestResponse(consultationRequest);
    }

    @Override
    @Transactional
    public ConsultationRequestResponse requestMoreInfo(Long actorId, UserRole actorRole, Long requestId, RequestMoreConsultationInfoRequest request) {
        validateConsultationManager(actorRole);
        ConsultationRequest consultationRequest = requestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));
        ConsultationFlowGuard.requireLegacy(consultationRequest);

        if (consultationRequest.getStatus() != ConsultationRequestStatus.PENDING_REVIEW)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);

        Instant now = Instant.now();
        consultationRequest.setStatus(ConsultationRequestStatus.NEED_MORE_INFO);
        consultationRequest.setMoreInfoReason(request.getReason());
        consultationRequest.setReviewedByAdminId(actorId);
        consultationRequest.setReviewedAt(now);

        moreInfoCycleRepository.save(ConsultationMoreInfoCycle.builder()
                .requestId(requestId)
                .requestedItemsCategory(request.getRequestedItemsCategory())
                .coordinatorMessage(request.getReason())
                .requestedBy(actorId)
                .requestedAt(now)
                .build());

        consultationRequest = requestRepository.save(consultationRequest);
        auditRequest(consultationRequest, BusinessEventType.REQUEST_MORE_INFO_REQUESTED, actorId, actorRole,
                ConsultationRequestStatus.PENDING_REVIEW, ConsultationRequestStatus.NEED_MORE_INFO,
                request.getReason(), NotificationType.REQUEST_NEEDS_INFO);
        return toRequestResponse(consultationRequest);
    }

    @Override
    @Transactional
    public ConsultationRequestResponse submitMoreInfo(Long memberId, Long requestId, SubmitConsultationMoreInfoRequest request) {
        ConsultationRequest consultationRequest = requestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));
        ConsultationFlowGuard.requireLegacy(consultationRequest);

        if (!consultationRequest.getMemberId().equals(memberId))
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);

        if (consultationRequest.getStatus() != ConsultationRequestStatus.NEED_MORE_INFO)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);
        if (consultationRequest.getIntakeFrozenAt() != null)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);

        List<Long> responseRecordIds = normalizeHealthRecordIds(
                request.getSelectedHealthRecordIds(),
                request.getHealthRecordId()
        );
        validateHealthRecordOwners(responseRecordIds, memberId);
        appendDistinct(consultationRequest.getSelectedHealthRecordIds(), responseRecordIds);
        if (consultationRequest.getHealthRecordId() == null && !responseRecordIds.isEmpty())
            consultationRequest.setHealthRecordId(responseRecordIds.getFirst());

        String responseNote = firstNonBlank(request.getResponseNote(), request.getAdditionalNote());
        ConsultationMoreInfoCycle cycle = moreInfoCycleRepository
                .findFirstByRequestIdAndRespondedAtIsNullOrderByRequestedAtDesc(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CONSULTATION_STATUS));
        Instant now = Instant.now();
        cycle.setMemberResponse(responseNote);
        cycle.setResponseHealthRecordIds(responseRecordIds);
        cycle.setRespondedAt(now);
        moreInfoCycleRepository.save(cycle);

        consultationRequest.setMemberAdditionalNote(responseNote);
        consultationRequest.setMoreInfoReason(null);
        consultationRequest.setStatus(ConsultationRequestStatus.PENDING_REVIEW);

        consultationRequest = requestRepository.save(consultationRequest);
        auditRequest(consultationRequest, BusinessEventType.REQUEST_MORE_INFO_SUBMITTED, memberId, UserRole.MEMBER,
                ConsultationRequestStatus.NEED_MORE_INFO, ConsultationRequestStatus.PENDING_REVIEW,
                null, NotificationType.REQUEST_RECEIVED);
        return toRequestResponse(consultationRequest);
    }

    @Override
    @Transactional
    public ConsultationRequestResponse cancelMyRequest(Long memberId, Long requestId) {
        ConsultationRequest consultationRequest = requestRepository.findById(requestId)
                .or(() -> requestRepository.findByIdForUpdate(requestId))
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));

        if (!consultationRequest.getMemberId().equals(memberId))
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);

        if (consultationRequest.getFlowType() == ConsultationFlowType.QUEUE_DISPATCH_V1) {
            doctorDispatchSelectionService.lockDispatchStateForOfferCommit();
            ConsultationQueueEntry entry = queueEntryRepository.findByRequestIdForUpdate(requestId)
                    .orElseThrow(() -> new AppException(ErrorCode.DATA_INTEGRITY_VIOLATION));
            return cancelQueueRequest(consultationRequest, entry, memberId);
        }

        consultationRequest = requestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));

        ConsultationFlowGuard.requireLegacy(consultationRequest);

        if (consultationRequest.getStatus() != ConsultationRequestStatus.PENDING_REVIEW
                && consultationRequest.getStatus() != ConsultationRequestStatus.NEED_MORE_INFO
                && consultationRequest.getStatus() != ConsultationRequestStatus.WAITING_ACCEPTANCE
                && consultationRequest.getStatus() != ConsultationRequestStatus.WAITING_PAYMENT)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);

        reservationService.release(consultationRequest, DoctorReservationReleaseReason.MEMBER_CANCELLED);
        agreementService.invalidateCurrent(requestId, "Member cancelled before care activation");
        consultationRequest.setStatus(ConsultationRequestStatus.CANCELLED);
        consultationRequest.setCancelledAt(Instant.now(clock));
        consultationRequest = requestRepository.save(consultationRequest);
        auditRequest(consultationRequest, BusinessEventType.REQUEST_CANCELLED, memberId, UserRole.MEMBER,
                null, ConsultationRequestStatus.CANCELLED, "Member cancelled before activation", NotificationType.AGREEMENT_INVALIDATED);
        paymentCancellationService.prepareRequestCancellation(requestId);
        paymentCancellationService.cancelProviderLinksAfterCommit(requestId);
        return toRequestResponse(consultationRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public CurrentQueueStateResponse getCurrentQueueState(Long memberId) {
        validateMember(memberId);
        Optional<ConsultationSession> activeSession = sessionRepository.findByMemberIdAndFlowTypeAndStatus(
                memberId, ConsultationFlowType.QUEUE_DISPATCH_V1, ConsultationStatus.ACTIVE);
        if (activeSession.isPresent()) return activeSessionState(activeSession.get());
        ConsultationQueueEntry entry = queueEntryRepository
                .findFirstByMemberIdAndStatusInOrderByQueueDateAscQueueNumberAsc(memberId, UNRESOLVED_QUEUE_STATUSES)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));
        ConsultationRequest request = requestRepository.findById(entry.getRequestId())
                .filter(candidate -> candidate.getMemberId().equals(memberId)
                        && candidate.getFlowType() == ConsultationFlowType.QUEUE_DISPATCH_V1)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));
        QueueStatistics statistics = queueStatistics();
        Optional<DoctorOffer> visibleOffer = queueMemberOffer(entry);
        long peopleAhead = queueEntryRepository.countUnresolvedAhead(
                UNRESOLVED_QUEUE_STATUSES, entry.getQueueDate(), entry.getQueueNumber());
        return CurrentQueueStateResponse.builder()
                .phase(entry.getStatus() == ConsultationQueueStatus.WAITING_MEMBER_CONFIRMATION
                        ? CurrentConsultationPhase.WAITING_CONFIRMATION : CurrentConsultationPhase.QUEUE)
                .requestId(request.getId())
                .queueEntryId(entry.getId())
                .queueNumber(entry.getQueueNumber())
                .queueDate(entry.getQueueDate())
                .queueStatus(entry.getStatus())
                .requestStatus(request.getStatus())
                .queuedAt(entry.getQueuedAt())
                .peopleAhead(peopleAhead)
                .doctorsOnDuty(statistics.doctorsOnDuty())
                .availableDoctors(statistics.availableDoctors())
                .busyDoctors(statistics.busyDoctors())
                .offerId(visibleOffer.map(DoctorOffer::offerId).orElse(null))
                .memberConfirmExpiresAt(visibleOffer.map(DoctorOffer::memberConfirmExpiresAt).orElse(null))
                .doctorReady(visibleOffer.map(o -> o.state() == DoctorOfferState.WAITING_MEMBER_CONFIRMATION).orElse(false))
                .doctorId(visibleOffer.filter(o -> o.state() == DoctorOfferState.WAITING_MEMBER_CONFIRMATION)
                        .map(DoctorOffer::doctorId).orElse(null))
                .build();
    }

    private CurrentQueueStateResponse activeSessionState(ConsultationSession session) {
        ConsultationRequest request = requestRepository.findById(session.getRequestId())
                .filter(candidate -> candidate.getMemberId().equals(session.getMemberId())
                        && candidate.getFlowType() == ConsultationFlowType.QUEUE_DISPATCH_V1
                        && candidate.getStatus() == ConsultationRequestStatus.FULFILLED)
                .orElseThrow(() -> new AppException(ErrorCode.DATA_INTEGRITY_VIOLATION));
        ConsultationQueueEntry entry = queueEntryRepository.findByRequestId(request.getId())
                .filter(candidate -> candidate.getStatus() == ConsultationQueueStatus.FULFILLED)
                .orElseThrow(() -> new AppException(ErrorCode.DATA_INTEGRITY_VIOLATION));
        return CurrentQueueStateResponse.builder()
                .phase(CurrentConsultationPhase.ACTIVE_SESSION)
                .requestId(request.getId()).queueEntryId(entry.getId())
                .queueNumber(entry.getQueueNumber()).queueDate(entry.getQueueDate())
                .queueStatus(entry.getStatus()).requestStatus(request.getStatus()).queuedAt(entry.getQueuedAt())
                .peopleAhead(0).doctorId(session.getDoctorId()).doctorReady(true)
                .sessionId(session.getId()).sessionStatus(session.getStatus())
                .sessionStartedAt(session.getStartedAt()).sessionEndsAt(session.getEndsAt()).build();
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultationQueueStatisticsResponse getQueueStatistics(Long memberId) {
        validateMember(memberId);
        QueueStatistics statistics = queueStatistics();
        return new ConsultationQueueStatisticsResponse(
                statistics.doctorsOnDuty(), statistics.availableDoctors(), statistics.busyDoctors(),
                queueEntryRepository.countByStatusIn(UNRESOLVED_QUEUE_STATUSES));
    }

    @Override
    public ConsultationRequestResponse getMyRequestById(Long memberId, Long requestId) {
        ConsultationRequest consultationRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));

        if (!consultationRequest.getMemberId().equals(memberId))
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);

        return toRequestResponse(consultationRequest);
    }

    @Override
    public PageResponse<ConsultationRequestResponse> getMyRequests(Long memberId, Pageable pageable) {
        Page<ConsultationRequestResponse> page = requestRepository
                .findByMemberIdOrderByCreatedAtDesc(memberId, pageable)
                .map(this::toRequestResponse);
        return new PageResponse<>(page);
    }

    @Override
    public PageResponse<ConsultationRequestResponse> getRequestsForAdmin(
            UserRole actorRole,
            ConsultationRequestStatus status,
            Long memberId,
            Long preferredDoctorId,
            Long assignedDoctorId,
            Instant fromDate,
            Instant toDate,
            Pageable pageable
    ) {
        validateConsultationManager(actorRole);
        Page<ConsultationRequestResponse> page = requestRepository
                .findAll(buildRequestFilter(status, memberId, preferredDoctorId, assignedDoctorId, fromDate, toDate), pageable)
                .map(this::toRequestResponse);
        return new PageResponse<>(page);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultationRequestReviewResponse getRequestReviewById(UserRole actorRole, Long requestId) {
        validateConsultationManager(actorRole);
        ConsultationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));
        return toReviewResponse(request);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DoctorCandidateResponse> getDoctorCandidates(
            UserRole actorRole,
            Long requestId,
            DoctorSpecialty specialty,
            String keyword,
            Boolean eligibleOnly,
            Pageable pageable
    ) {
        validateConsultationManager(actorRole);
        ConsultationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_REQUEST_NOT_FOUND));

        String normalizedKeyword = trimToNull(keyword);
        Page<UserAccount> doctors = normalizedKeyword == null
                ? userAccountRepository.findDoctors(UserRole.DOCTOR, AccountStatus.ACTIVE, pageable)
                : userAccountRepository.searchDoctors(
                UserRole.DOCTOR,
                AccountStatus.ACTIVE,
                normalizedKeyword,
                pageable
        );
        List<Long> doctorIds = doctors.stream().map(UserAccount::getId).toList();
        Map<Long, DoctorCareProfile> profiles = doctorCareProfileRepository.findByDoctorIdIn(doctorIds)
                .stream()
                .collect(Collectors.toMap(DoctorCareProfile::getDoctorId, Function.identity()));

        List<DoctorCandidateResponse> candidates = doctors.stream()
                .map(doctor -> toCandidateResponse(request, doctor, profiles.get(doctor.getId())))
                .filter(candidate -> specialty == null || specialty == candidate.getSpecialty())
                .filter(candidate -> !Boolean.TRUE.equals(eligibleOnly) || Boolean.TRUE.equals(candidate.getEligible()))
                .toList();

        return new PageResponse<>(new PageImpl<>(candidates, pageable, candidates.size()));
    }

    @Override
    @Transactional
    public void expireWaitingPaymentRequests(UserRole actorRole) {
        validateConsultationManager(actorRole);
        Instant now = Instant.now();
        List<ConsultationRequest> expiredRequests = requestRepository
                .findByStatusInAndPaymentDeadlineBefore(ACTIVE_RESERVATION_STATUSES, now);
        reservationService.expireOverdueReservations(now);
        expiredRequests.forEach(request -> {
                    request.setStatus(ConsultationRequestStatus.EXPIRED);
                    request.setExpiredAt(now);
                    agreementService.invalidateCurrent(request.getId(), "Offer/payment window expired");
                    requestRepository.save(request);
                    auditRequest(request, BusinessEventType.REQUEST_EXPIRED, null, null,
                            null, ConsultationRequestStatus.EXPIRED, "Offer/payment window expired", NotificationType.PAYMENT_FAILED);
                });
    }

    private ConsultationRequestResponse cancelQueueRequest(
            ConsultationRequest request, ConsultationQueueEntry entry, Long memberId) {
        // Queue cancellation is idempotent when both persisted states already agree.
        if (request.getStatus() == ConsultationRequestStatus.CANCELLED
                && entry.getStatus() == ConsultationQueueStatus.CANCELLED)
            return toRequestResponse(request);

        if (request.getStatus() != ConsultationRequestStatus.QUEUED
                || !UNRESOLVED_QUEUE_STATUSES.contains(entry.getStatus()))
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);

        Optional<DoctorOffer> activeOffer = doctorOfferStore.findByQueueEntryId(entry.getId());
        if (entry.getStatus() != ConsultationQueueStatus.WAITING && activeOffer.isEmpty())
            throw new AppException(ErrorCode.DISPATCH_TEMPORARILY_UNAVAILABLE,
                    "The active Redis offer cannot be verified safely");
        if (activeOffer.isPresent() && !doctorOfferStore.release(activeOffer.get()))
            throw new AppException(ErrorCode.CONSULTATION_OFFER_STALE);

        Instant now = Instant.now(clock);
        request.setStatus(ConsultationRequestStatus.CANCELLED);
        request.setCancelledAt(now);
        entry.setStatus(ConsultationQueueStatus.CANCELLED);
        entry.setCancelledAt(now);
        request = requestRepository.save(request);
        queueEntryRepository.save(entry);
        auditQueueCancellation(request, entry, memberId);
        applicationEventPublisher.publishEvent(new DispatchRequested("member-cancelled"));
        return toRequestResponse(request);
    }

    private QueueStatistics queueStatistics() {
        long persistedAvailable = doctorCareProfileRepository.countByDispatchStatus(DoctorDispatchStatus.AVAILABLE);
        long available;
        try {
            available = doctorDispatchSelectionService.countEffectivelyDispatchableDoctors();
        } catch (AppException ex) {
            if (ex.getErrorCode() != ErrorCode.DISPATCH_TEMPORARILY_UNAVAILABLE) throw ex;
            available = 0; // Redis failure: fail closed rather than claim a held Doctor is dispatchable.
        }
        long busy = doctorCareProfileRepository.countByDispatchStatus(DoctorDispatchStatus.BUSY);
        return new QueueStatistics(persistedAvailable + busy, available, busy);
    }

    private void auditQueueAdmission(ConsultationRequest request, ConsultationQueueEntry entry, Long memberId) {
        NotificationIntent acknowledgement = new NotificationIntent(memberId, UserRole.MEMBER,
                NotificationType.REQUEST_RECEIVED,
                "Care request queued",
                "Your care request was received and added to the consultation queue.",
                BusinessDomainType.REQUEST,
                request.getId(),
                "queue:" + entry.getId() + ":queued:member");
        OperationalEventPublisher.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.REQUEST)
                .domainId(request.getId())
                .eventType(BusinessEventType.REQUEST_QUEUED)
                .actorType(BusinessActorType.USER)
                .actorUserId(memberId)
                .actorRole(UserRole.MEMBER.name())
                .requestId(request.getId())
                .memberId(memberId)
                .newState(ConsultationRequestStatus.QUEUED.name())
                .metadata(Map.of(
                        "queueEntryId", entry.getId().toString(),
                        "queueNumber", entry.getQueueNumber().toString(),
                        "queueDate", entry.getQueueDate().toString()))
                .idempotencyKey("queue:" + entry.getId() + ":queued")
                .occurredAt(entry.getQueuedAt())
                .notifications(List.of(acknowledgement))
                .build());
    }

    private void auditQueueCancellation(ConsultationRequest request, ConsultationQueueEntry entry, Long memberId) {
        OperationalEventPublisher.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.REQUEST)
                .domainId(request.getId())
                .eventType(BusinessEventType.QUEUE_CANCELLED)
                .actorType(BusinessActorType.USER)
                .actorUserId(memberId)
                .actorRole(UserRole.MEMBER.name())
                .requestId(request.getId())
                .memberId(memberId)
                .previousState(ConsultationRequestStatus.QUEUED.name())
                .newState(ConsultationRequestStatus.CANCELLED.name())
                .reason("Member left consultation queue")
                .metadata(Map.of("queueEntryId", entry.getId().toString()))
                .idempotencyKey("queue:" + entry.getId() + ":cancelled")
                .occurredAt(entry.getCancelledAt())
                .notifications(List.of())
                .build());
    }

    private record QueueStatistics(long doctorsOnDuty, long availableDoctors, long busyDoctors) {
    }

    private Optional<DoctorOffer> queueMemberOffer(ConsultationQueueEntry entry) {
        if (entry.getStatus() != ConsultationQueueStatus.WAITING_MEMBER_CONFIRMATION) return Optional.empty();
        try { return doctorOfferStore.findByQueueEntryId(entry.getId()); }
        catch (AppException ex) {
            if (ex.getErrorCode() == ErrorCode.DISPATCH_TEMPORARILY_UNAVAILABLE) return Optional.empty();
            throw ex;
        }
    }

    private void auditRequest(ConsultationRequest request, BusinessEventType eventType, Long actorId,
            UserRole actorRole, ConsultationRequestStatus previous, ConsultationRequestStatus next,
            String reason, NotificationType notificationType) {
        List<NotificationIntent> notifications = new ArrayList<>();
        if (notificationType != null)
            notifications.add(new NotificationIntent(request.getMemberId(), notificationType,
                    requestNotificationTitle(notificationType), requestNotificationMessage(notificationType),
                    BusinessDomainType.REQUEST, request.getId(),
                    "request:" + request.getId() + ":" + eventType + ":member"));
        if (eventType == BusinessEventType.REQUEST_CREATED
                || eventType == BusinessEventType.REQUEST_MORE_INFO_SUBMITTED)
            notifications.add(NotificationIntent.forRole(UserRole.CARE_COORDINATOR,
                    NotificationType.REQUEST_RECEIVED,
                    eventType == BusinessEventType.REQUEST_CREATED ? "New care request" : "Care request resubmitted",
                    "A care request is ready for coordinator review.", BusinessDomainType.REQUEST, request.getId(),
                    "request:" + request.getId() + ":" + eventType + ":coordinators"));
        OperationalEventPublisher.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.REQUEST).domainId(request.getId()).eventType(eventType)
                .actorType(actorId == null ? BusinessActorType.SYSTEM : BusinessActorType.USER)
                .actorUserId(actorId).actorRole(actorRole == null ? null : actorRole.name())
                .requestId(request.getId()).memberId(request.getMemberId()).doctorId(request.getAssignedDoctorId())
                .previousState(previous == null ? null : previous.name()).newState(next == null ? null : next.name())
                .reason(reason).idempotencyKey("request:" + request.getId() + ":" + eventType + ":" + next)
                .notifications(notifications)
                .build());
    }

    private String requestNotificationTitle(NotificationType type) {
        return switch (type) {
            case REQUEST_NEEDS_INFO -> "More information needed";
            case REQUEST_REJECTED -> "Care request update";
            case AGREEMENT_READY -> "Care agreement ready";
            case AGREEMENT_INVALIDATED -> "Care request cancelled";
            case PAYMENT_FAILED -> "Care offer expired";
            default -> "Care request received";
        };
    }

    private String requestNotificationMessage(NotificationType type) {
        return switch (type) {
            case REQUEST_NEEDS_INFO -> "Your care coordinator requested additional information.";
            case REQUEST_REJECTED -> "Your care request was not approved. Review the request for details.";
            case AGREEMENT_READY -> "A care agreement is ready for your review and acceptance.";
            case AGREEMENT_INVALIDATED -> "Your care request was cancelled before activation.";
            case PAYMENT_FAILED -> "The care offer and payment window expired.";
            default -> "Your care request was submitted for review.";
        };
    }

    private void validateConsultationManager(UserRole actorRole) {
        if (actorRole == UserRole.SUPER_ADMIN
                || actorRole == UserRole.ADMIN
                || actorRole == UserRole.CARE_COORDINATOR)
            return;
        throw new AppException(ErrorCode.ACCESS_DENIED, "You are not allowed to manage consultation requests");
    }

    private void validateMember(Long memberId) {
        UserAccount member = userAccountRepository.findById(memberId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
        if (member.getRole() != UserRole.MEMBER)
            throw new AppException(ErrorCode.MEMBER_NOT_FOUND);
    }

    private CareServicePackage findActivePackage(Long packageId) {
        return packageRepository.findByIdAndStatus(packageId, CareServicePackageStatus.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.CARE_SERVICE_PACKAGE_NOT_FOUND));
    }

    private long getDoctorEffectiveLoad(Long doctorId, Instant now) {
        return reservationService.getEffectiveLoad(doctorId, now);
    }

    private DoctorCandidateResponse toCandidateResponse(ConsultationRequest request, UserAccount doctor, DoctorCareProfile profile) {
        Instant now = Instant.now();
        long effectiveLoad = getDoctorEffectiveLoad(doctor.getId(), now);
        List<DoctorIneligibilityReason> reasons = reservationService.getIneligibilityReasons(
                request, doctor, now, null);

        return DoctorCandidateResponse.builder()
                .doctorId(doctor.getId())
                .email(doctor.getEmail())
                .displayName(doctor.getProfile() == null ? null : doctor.getProfile().getDisplayName())
                .phone(doctor.getProfile() == null ? null : doctor.getProfile().getPhone())
                .specialty(profile == null ? null : profile.getSpecialty())
                .acceptsOneOnOneCare(profile == null ? null : profile.getAcceptsOneOnOneCare())
                .effectiveLoad(effectiveLoad)
                .maxActiveConsultations(profile == null ? null : profile.getMaxActiveConsultations())
                .declaredSupportSchedule(profile == null ? null : profile.getAvailabilityJson())
                .timezone(profile == null ? null : profile.getTimezone())
                .preferredByMember(Objects.equals(request.getPreferredDoctorId(), doctor.getId()))
                .eligible(reasons.isEmpty())
                .ineligibleReasons(reasons)
                .build();
    }

    private ConsultationRequestReviewResponse toReviewResponse(ConsultationRequest request) {
        return ConsultationRequestReviewResponse.builder()
                .id(request.getId())
                .status(request.getStatus())
                .reason(request.getReason())
                .reasonForCare(request.getReasonForCare())
                .currentConcern(request.getCurrentConcern())
                .careGoal(request.getCareGoal())
                .memberNote(request.getMemberNote())
                .relevantSelfReportedContext(request.getRelevantSelfReportedContext())
                .selectedHealthRecordIds(request.getSelectedHealthRecordIds())
                .selectedHealthRecords(healthRecordSummaries(request.getSelectedHealthRecordIds(), request.getMemberId()))
                .intakeFrozenAt(request.getIntakeFrozenAt())
                .moreInfoHistory(moreInfoHistory(request.getId(), request.getMemberId()))
                .packageId(request.getPackageId())
                .packagePriceSnapshot(request.getPackagePriceSnapshot())
                .packageDurationDaysSnapshot(request.getPackageDurationDaysSnapshot())
                .member(userSummary(request.getMemberId()))
                .preferredDoctor(userSummary(request.getPreferredDoctorId()))
                .assignedDoctor(userSummary(request.getAssignedDoctorId()))
                .healthRecord(healthRecordSummary(request.getHealthRecordId(), request.getMemberId()))
                .moreInfoReason(request.getMoreInfoReason())
                .memberAdditionalNote(request.getMemberAdditionalNote())
                .assignedDoctorId(request.getAssignedDoctorId())
                .doctorReservedAt(request.getDoctorReservedAt())
                .paymentDeadline(request.getPaymentDeadline())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }

    private UserSummaryResponse userSummary(Long userId) {
        if (userId == null)
            return null;
        return userAccountRepository.findById(userId)
                .map(user -> {
                    UserProfile profile = user.getProfile();
                    return UserSummaryResponse.builder()
                            .userId(user.getId())
                            .email(user.getEmail())
                            .displayName(profile == null ? null : profile.getDisplayName())
                            .phone(profile == null ? null : profile.getPhone())
                            .build();
                })
                .orElse(null);
    }

    private HealthRecordSummaryResponse healthRecordSummary(Long healthRecordId, Long memberId) {
        if (healthRecordId == null)
            return null;
        return healthRecordRepository.findByIdAndUserId(healthRecordId, memberId)
                .map(this::toHealthRecordSummary)
                .orElse(null);
    }

    private List<HealthRecordSummaryResponse> healthRecordSummaries(List<Long> healthRecordIds, Long memberId) {
        if (healthRecordIds == null || healthRecordIds.isEmpty())
            return List.of();
        return healthRecordIds.stream()
                .map(id -> healthRecordSummary(id, memberId))
                .filter(Objects::nonNull)
                .toList();
    }

    private List<ConsultationMoreInfoCycleResponse> moreInfoHistory(Long requestId, Long memberId) {
        if (requestId == null)
            return List.of();
        return moreInfoCycleRepository.findByRequestIdOrderByRequestedAtAsc(requestId).stream()
                .map(cycle -> ConsultationMoreInfoCycleResponse.builder()
                        .id(cycle.getId())
                        .requestedItemsCategory(cycle.getRequestedItemsCategory())
                        .coordinatorMessage(cycle.getCoordinatorMessage())
                        .requestedBy(cycle.getRequestedBy())
                        .requestedAt(cycle.getRequestedAt())
                        .memberResponse(cycle.getMemberResponse())
                        .responseHealthRecordIds(cycle.getResponseHealthRecordIds())
                        .responseHealthRecords(healthRecordSummaries(cycle.getResponseHealthRecordIds(), memberId))
                        .respondedAt(cycle.getRespondedAt())
                        .build())
                .toList();
    }

    private ConsultationRequestResponse toRequestResponse(ConsultationRequest request) {
        ConsultationRequestResponse response = mapper.toRequestResponse(request);
        response.setSelectedHealthRecords(healthRecordSummaries(request.getSelectedHealthRecordIds(), request.getMemberId()));
        response.setMoreInfoHistory(moreInfoHistory(request.getId(), request.getMemberId()));
        if (request.getFlowType() == ConsultationFlowType.QUEUE_DISPATCH_V1) {
            queueEntryRepository.findByRequestId(request.getId()).ifPresent(entry -> {
                response.setQueueEntryId(entry.getId());
                response.setQueueNumber(entry.getQueueNumber());
                response.setQueueDate(entry.getQueueDate());
                response.setQueueStatus(entry.getStatus());
                response.setQueuedAt(entry.getQueuedAt());
            });
        }
        return response;
    }

    private HealthRecordSummaryResponse toHealthRecordSummary(HealthRecord record) {
        return HealthRecordSummaryResponse.builder()
                .recordId(record.getId())
                .status(record.getStatus())
                .predictionLabel(record.getPredictionLabel())
                .confidence(record.getConfidence())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }

    private Specification<ConsultationRequest> buildRequestFilter(
            ConsultationRequestStatus status,
            Long memberId,
            Long preferredDoctorId,
            Long assignedDoctorId,
            Instant fromDate,
            Instant toDate
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null)
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            if (memberId != null)
                predicates.add(criteriaBuilder.equal(root.get("memberId"), memberId));
            if (preferredDoctorId != null)
                predicates.add(criteriaBuilder.equal(root.get("preferredDoctorId"), preferredDoctorId));
            if (assignedDoctorId != null)
                predicates.add(criteriaBuilder.equal(root.get("assignedDoctorId"), assignedDoctorId));
            if (fromDate != null)
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
            if (toDate != null)
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), toDate));
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void validateHealthRecordOwners(List<Long> healthRecordIds, Long memberId) {
        healthRecordIds.forEach(id -> healthRecordRepository.findByIdAndUserId(id, memberId)
                .orElseThrow(() -> new AppException(ErrorCode.HEALTH_RECORD_NOT_FOUND)));
    }

    private List<Long> normalizeHealthRecordIds(List<Long> selectedIds, Long legacyId) {
        LinkedHashSet<Long> normalized = new LinkedHashSet<>();
        if (selectedIds != null)
            selectedIds.stream().filter(Objects::nonNull).forEach(normalized::add);
        if (legacyId != null)
            normalized.add(legacyId);
        return new ArrayList<>(normalized);
    }

    private void appendDistinct(List<Long> target, List<Long> additions) {
        additions.forEach(id -> {
            if (!target.contains(id))
                target.add(id);
        });
    }

    private String firstNonBlank(String preferred, String fallback) {
        String normalized = trimToNull(preferred);
        return normalized == null ? trimToNull(fallback) : normalized;
    }

    private String trimToNull(String value) {
        if (value == null)
            return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
