package fit.iuh.se.hschat.service.session.impl;

import fit.iuh.se.hschat.dto.request.AdminCreateConsultationSessionRequest;
import fit.iuh.se.hschat.dto.request.CloseConsultationRequest;
import fit.iuh.se.hschat.dto.request.ExtendConsultationRequest;
import fit.iuh.se.hschat.dto.request.RequestSessionTerminationRequest;
import fit.iuh.se.hschat.dto.response.ConsultationSessionResponse;
import fit.iuh.se.hschat.entity.CareServicePackage;
import fit.iuh.se.hschat.entity.ConsultationParticipant;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.DoctorCareProfile;
import fit.iuh.se.hschat.entity.enums.CareServicePackageStatus;
import fit.iuh.se.hschat.entity.enums.CareOperationalReviewReason;
import fit.iuh.se.hschat.entity.enums.CareTerminationReason;
import fit.iuh.se.hschat.entity.enums.ConsultationCompletionReason;
import fit.iuh.se.hschat.entity.enums.ConsultationParticipantRole;
import fit.iuh.se.hschat.entity.enums.ConsultationSourceType;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.mapper.ConsultationMapper;
import fit.iuh.se.hschat.repository.CareServicePackageRepository;
import fit.iuh.se.hschat.repository.ConsultationParticipantRepository;
import fit.iuh.se.hschat.repository.ConsultationMessageRepository;
import fit.iuh.se.hschat.repository.ConsultationRequestRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.repository.DoctorCareProfileRepository;
import fit.iuh.se.hschat.service.doctor.SupportScheduleValidator;
import fit.iuh.se.hschat.service.authorization.EpisodeHealthRecordAuthorizationService;
import fit.iuh.se.hschat.service.reservation.DoctorReservationService;
import fit.iuh.se.hschat.service.finalsummary.FinalSummaryClosureService;
import fit.iuh.se.hschat.service.session.ConsultationSessionService;
import fit.iuh.se.hschat.service.renewal.ConsultationRenewalService;
import fit.iuh.se.hshealthrecord.repository.HealthRecordRepository;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import fit.iuh.se.hsoperations.dto.command.*;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.event.OperationalEventPublisher;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConsultationSessionServiceImpl implements ConsultationSessionService {

    static final List<ConsultationStatus> MEMBER_BUSY_SESSION_STATUSES = List.of(
            ConsultationStatus.SCHEDULED,
            ConsultationStatus.ACTIVE
    );

    ConsultationSessionRepository sessionRepository;
    ConsultationParticipantRepository participantRepository;
    ConsultationMessageRepository messageRepository;
    HealthRecordRepository healthRecordRepository;
    UserAccountRepository userAccountRepository;
    ConsultationRequestRepository requestRepository;
    CareServicePackageRepository packageRepository;
    DoctorCareProfileRepository doctorCareProfileRepository;
    SupportScheduleValidator scheduleValidator;
    DoctorReservationService reservationService;
    EpisodeHealthRecordAuthorizationService authorizationService;
    FinalSummaryClosureService finalSummaryClosureService;
    ConsultationRenewalService renewalService;
    ConsultationMapper mapper;
    OperationalEventPublisher OperationalEventPublisher;

    @NonFinal
    @Value("${app.consultation.default-doctor-max-active-sessions:20}")
    int defaultDoctorMaxActiveSessions;

    @Override
    @Transactional
    public ConsultationSessionResponse createSessionByAdmin(Long actorId, UserRole actorRole, AdminCreateConsultationSessionRequest request) {
        if (actorRole != UserRole.ADMIN && actorRole != UserRole.SUPER_ADMIN)
            throw new AppException(ErrorCode.ACCESS_DENIED,
                    "Only Admin or Super Admin may use exceptional care activation override");
        if (request.getOverrideReason() == null || request.getOverrideReason().isBlank())
            throw new AppException(ErrorCode.INVALID_PARAMETER, "Exceptional override reason is required");
        if (request.getServiceScope() == null || request.getServiceScope().isBlank())
            throw new AppException(ErrorCode.INVALID_PARAMETER, "Exceptional override service scope is required");
        log.info("Creating consultation session by actor {} with role {} for member {} and doctor {}",
                actorId, actorRole, request.getMemberId(), request.getDoctorId());

        validateMember(request.getMemberId());
        DoctorCareProfile doctorProfile = validateDoctorForConsultation(request.getDoctorId());
        validateHealthRecordOwner(request.getHealthRecordId(), request.getMemberId());
        validateConsultationPeriod(request.getEndsAt(), request.getSupportEndsAt());

        if (sessionRepository.existsByMemberIdAndStatusIn(request.getMemberId(), MEMBER_BUSY_SESSION_STATUSES))
            throw new AppException(ErrorCode.MEMBER_ALREADY_HAS_ACTIVE_CONSULTATION);

        Instant now = Instant.now();
        if (getDoctorEffectiveLoad(request.getDoctorId(), now) >= doctorProfile.getMaxActiveConsultations())
            throw new AppException(ErrorCode.DOCTOR_CAPACITY_EXCEEDED);

        Instant startedAt = request.getStartedAt() == null ? now : request.getStartedAt();
        Instant supportEndsAt = request.getSupportEndsAt() == null ? request.getEndsAt() : request.getSupportEndsAt();
        ConsultationStatus status = startedAt.isAfter(now) ? ConsultationStatus.SCHEDULED : ConsultationStatus.ACTIVE;
        CareServicePackage carePackage = findOptionalActivePackage(request.getPackageId());

        ConsultationSession session = ConsultationSession.builder()
                .memberId(request.getMemberId())
                .doctorId(request.getDoctorId())
                .createdByAdminId(actorId)
                .exceptionalOverride(true)
                .overrideReason(request.getOverrideReason().trim())
                .overrideServiceScope(request.getServiceScope().trim())
                .sourceType(ConsultationSourceType.ADMIN_CREATED)
                .status(status)
                .startedAt(startedAt)
                .activatedAt(status == ConsultationStatus.ACTIVE ? now : null)
                .endsAt(request.getEndsAt())
                .supportEndsAt(supportEndsAt)
                .supportScheduleSnapshotJson(doctorProfile.getAvailabilityJson())
                .supportTimezoneSnapshot(doctorProfile.getTimezone())
                .packageId(carePackage == null ? null : carePackage.getId())
                .packageVersion(carePackage == null ? null : carePackage.getVersionNumber())
                .packagePriceSnapshot(carePackage == null ? null : carePackage.getPriceAmount())
                .packageDurationDaysSnapshot(carePackage == null ? null : carePackage.getDurationDays())
                .healthRecordId(request.getHealthRecordId())
                .build();

        session = sessionRepository.save(session);

        participantRepository.save(ConsultationParticipant.builder()
                .sessionId(session.getId())
                .userId(request.getMemberId())
                .role(ConsultationParticipantRole.MEMBER)
                .joinedAt(now)
                .active(true)
                .build());

        participantRepository.save(ConsultationParticipant.builder()
                .sessionId(session.getId())
                .userId(request.getDoctorId())
                .role(ConsultationParticipantRole.DOCTOR)
                .joinedAt(now)
                .active(true)
                .build());

        if (status == ConsultationStatus.ACTIVE && request.getHealthRecordId() != null)
            authorizationService.authorizeAdminInitialRecord(session, request.getHealthRecordId(), actorId);

        auditSession(session, BusinessEventType.SESSION_OVERRIDE_CREATED, actorId, actorRole, null, status,
                request.getOverrideReason(), null, status == ConsultationStatus.ACTIVE ? NotificationType.CARE_ACTIVATED : null);

        return toSessionResponse(session);
    }

    @Override
    public ConsultationSessionResponse getSessionById(Long userId, Long sessionId) {
        if (!participantRepository.existsBySessionIdAndUserIdAndActiveTrue(sessionId, userId))
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);

        ConsultationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_NOT_FOUND));
        ConsultationSessionResponse response = toSessionResponse(session, userId);
        if (response != null)
            enrichParticipantDisplayNames(List.of(response));
        return response;
    }

    @Override
    public PageResponse<ConsultationSessionResponse> getMySessions(Long userId, Pageable pageable) {
        Page<ConsultationSessionResponse> page = sessionRepository
                .findByMemberIdOrderByLastMessageAtDesc(userId, pageable)
                .map(session -> toSessionResponse(session, userId));
        return toPageResponse(page, pageable);
    }

    @Override
    public PageResponse<ConsultationSessionResponse> getDoctorSessions(Long doctorId, Pageable pageable) {
        Page<ConsultationSessionResponse> page = sessionRepository
                .findByDoctorIdOrderByLastMessageAtDesc(doctorId, pageable)
                .map(session -> toSessionResponse(session, doctorId));
        return toPageResponse(page, pageable);
    }

    @Override
    public PageResponse<ConsultationSessionResponse> getSessionsForAdmin(UserRole actorRole, Pageable pageable) {
        validateConsultationManager(actorRole);
        Page<ConsultationSessionResponse> page = sessionRepository
                .findAll(pageable)
                .map(mapper::toSessionResponse);
        return toPageResponse(page, pageable);
    }

    @Override
    public ConsultationSessionResponse extendSession(Long actorId, UserRole actorRole, Long sessionId, ExtendConsultationRequest request) {
        validateConsultationManager(actorRole);
        throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS,
                "Direct extension is disabled; use the Renewal Agreement and verified payment workflow");
    }

    @Override
    @Transactional
    public ConsultationSessionResponse closeSession(Long actorId, UserRole actorRole, Long sessionId, CloseConsultationRequest request) {
        validateConsultationManager(actorRole);
        ConsultationSession session = sessionRepository.findByIdForUpdate(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_NOT_FOUND));

        if (session.getStatus() != ConsultationStatus.ACTIVE
                && session.getStatus() != ConsultationStatus.SCHEDULED)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);

        Instant now = Instant.now();
        boolean meaningfulCare = session.getActivatedAt() != null
                && !Boolean.FALSE.equals(request.getMeaningfulCareOccurred());
        session.setStatus(ConsultationStatus.CANCELLED);
        session.setClosedAt(now);
        session.setCompletedAt(now);
        session.setCompletionReason(ConsultationCompletionReason.ADMINISTRATIVE_CANCELLATION);
        session.setCloseReason(request.getCloseReason());
        session.setTerminationReason(request.getTerminationReason() == null
                ? CareTerminationReason.ADMINISTRATIVE_CLOSURE : request.getTerminationReason());
        session.setTerminationDecidedBy(actorId);
        session.setTerminationDecidedByRole(actorRole);
        session.setTerminationDecidedAt(now);
        session.setMeaningfulCareOccurred(meaningfulCare);
        session.setOperationalReviewRequired(false);
        session.setOperationalReviewReason(null);
        session.setOperationalReviewFlaggedAt(null);

        session = sessionRepository.save(session);
        renewalService.cancelUnresolvedForClosedSession(sessionId,
                "Renewal cancelled because the active care episode was closed", now);
        if (meaningfulCare)
            finalSummaryClosureService.onSessionCompleted(session, now);
        auditSession(session, BusinessEventType.SESSION_CANCELLED, actorId, actorRole, ConsultationStatus.ACTIVE,
                ConsultationStatus.CANCELLED, request.getCloseReason(), null, NotificationType.CARE_CANCELLED);
        return toSessionResponse(session);
    }

    @Override
    @Transactional
    public ConsultationSessionResponse requestTermination(
            Long actorId, UserRole actorRole, Long sessionId, RequestSessionTerminationRequest request) {
        if (actorRole != UserRole.MEMBER && actorRole != UserRole.DOCTOR)
            throw new AppException(ErrorCode.ACCESS_DENIED,
                    "Only the assigned Member or Doctor may request care termination");
        ConsultationSession session = sessionRepository.findByIdForUpdate(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_NOT_FOUND));
        boolean assigned = actorRole == UserRole.MEMBER
                ? session.getMemberId().equals(actorId) : session.getDoctorId().equals(actorId);
        if (!assigned) throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);
        if (session.getStatus() != ConsultationStatus.ACTIVE)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);

        session.setTerminationReason(request.getReason());
        session.setTerminationRequestedBy(actorId);
        session.setTerminationRequestedByRole(actorRole);
        session.setTerminationRequestedAt(Instant.now());
        session.setCloseReason(request.getDetails().trim());
        session.setOperationalReviewRequired(true);
        session.setOperationalReviewReason(actorRole == UserRole.DOCTOR
                ? CareOperationalReviewReason.DOCTOR_TERMINATION_REQUESTED
                : CareOperationalReviewReason.MEMBER_TERMINATION_REQUESTED);
        session.setOperationalReviewFlaggedAt(Instant.now());
        session = sessionRepository.save(session);
        auditSession(session, BusinessEventType.SESSION_TERMINATION_REQUESTED, actorId, actorRole,
                ConsultationStatus.ACTIVE, ConsultationStatus.ACTIVE, request.getDetails(),
                new NeedsActionIntent(NeedsActionType.TERMINATION_REVIEW, NeedsActionPriority.HIGH,
                        "Care termination review", "An active-care participant requested termination.",
                        BusinessDomainType.SESSION, session.getId(), UserRole.CARE_COORDINATOR.name(),
                        "session:" + session.getId() + ":termination-review"),
                NotificationType.CARE_TERMINATION_REQUESTED);
        return toSessionResponse(session);
    }

    @Override
    @Transactional
    public void flagDisabledActiveParticipantsForReview(UserRole actorRole) {
        validateConsultationManager(actorRole);
        sessionRepository.findByStatusOrderByCreatedAtDesc(ConsultationStatus.ACTIVE, Pageable.unpaged())
                .forEach(candidate -> sessionRepository.findByIdForUpdate(candidate.getId()).ifPresent(session -> {
                    if (session.getStatus() != ConsultationStatus.ACTIVE) return;
                    AccountStatus doctorStatus = userAccountRepository.findById(session.getDoctorId())
                            .map(UserAccount::getStatus).orElse(AccountStatus.INACTIVE);
                    AccountStatus memberStatus = userAccountRepository.findById(session.getMemberId())
                            .map(UserAccount::getStatus).orElse(AccountStatus.INACTIVE);
                    CareOperationalReviewReason reason = doctorStatus != AccountStatus.ACTIVE
                            ? CareOperationalReviewReason.DOCTOR_ACCOUNT_DISABLED
                            : memberStatus != AccountStatus.ACTIVE
                            ? CareOperationalReviewReason.MEMBER_ACCOUNT_DISABLED : null;
                    if (reason == null) return;
                    session.setOperationalReviewRequired(true);
                    session.setOperationalReviewReason(reason);
                    session.setOperationalReviewFlaggedAt(Instant.now());
                    sessionRepository.save(session);
                    NeedsActionType type = reason == CareOperationalReviewReason.DOCTOR_ACCOUNT_DISABLED
                            ? NeedsActionType.DOCTOR_ACTIVE_CARE_INTERRUPTION : NeedsActionType.MEMBER_ACTIVE_CARE_INTERRUPTION;
                    auditSession(session, BusinessEventType.SESSION_PARTICIPANT_INTERRUPTED, null, null,
                            ConsultationStatus.ACTIVE, ConsultationStatus.ACTIVE, reason.name(),
                            new NeedsActionIntent(type, NeedsActionPriority.CRITICAL,
                                    "Active care interruption", "A disabled participant requires operational review.",
                                    BusinessDomainType.SESSION, session.getId(), UserRole.CARE_COORDINATOR.name(),
                                    "session:" + session.getId() + ":interruption:" + reason),
                            NotificationType.OPERATIONAL_REVIEW_REQUIRED);
                }));
    }

    @Override
    @Transactional
    public void expireOverdueSessions(UserRole actorRole) {
        validateConsultationManager(actorRole);
        Instant now = Instant.now();
        sessionRepository.findByStatusAndEndsAtBetween(
                        ConsultationStatus.ACTIVE, now, now.plus(24, ChronoUnit.HOURS))
                .forEach(session -> auditSession(session, BusinessEventType.SESSION_ENDING_SOON, null, null,
                        ConsultationStatus.ACTIVE, ConsultationStatus.ACTIVE, null, null,
                        NotificationType.CARE_ENDING));
        sessionRepository.findByStatusAndEndsAtBefore(ConsultationStatus.ACTIVE, now)
                .forEach(candidate -> sessionRepository.findByIdForUpdate(candidate.getId()).ifPresent(session -> {
                    if (session.getStatus() != ConsultationStatus.ACTIVE
                            || session.getEndsAt() == null || session.getEndsAt().isAfter(now))
                        return;
                    session.setStatus(ConsultationStatus.COMPLETED);
                    session.setCompletedAt(now);
                    session.setCompletionReason(ConsultationCompletionReason.PERIOD_ENDED);
                    session.setCloseReason("Consultation session completed automatically because the care period ended");
                    sessionRepository.save(session);
                    finalSummaryClosureService.onSessionCompleted(session, now);
                    auditSession(session, BusinessEventType.SESSION_COMPLETED, null, null,
                            ConsultationStatus.ACTIVE, ConsultationStatus.COMPLETED, session.getCloseReason(),
                            null, NotificationType.CARE_COMPLETED);
                }));
        finalSummaryClosureService.refreshOpenClosures(now);
    }

    @Override
    @Transactional
    public void activateScheduledSessions(UserRole actorRole) {
        validateConsultationManager(actorRole);
        Instant now = Instant.now();
        sessionRepository.findByStatusAndStartedAtBefore(ConsultationStatus.SCHEDULED, now)
                .forEach(session -> {
                    session.setStatus(ConsultationStatus.ACTIVE);
                    session.setActivatedAt(now);
                    sessionRepository.save(session);
                    if (session.getHealthRecordId() != null)
                        authorizationService.authorizeAdminInitialRecord(
                                session, session.getHealthRecordId(), session.getCreatedByAdminId());
                    auditSession(session, BusinessEventType.SESSION_ACTIVATED, null, null,
                            ConsultationStatus.SCHEDULED, ConsultationStatus.ACTIVE, null, null,
                            NotificationType.CARE_ACTIVATED);
                });
    }

    private void auditSession(ConsultationSession session, BusinessEventType eventType, Long actorId,
            UserRole actorRole, ConsultationStatus previous, ConsultationStatus next, String reason,
            NeedsActionIntent needsAction, NotificationType notificationType) {
        String key = "session:" + session.getId() + ":" + eventType + ":" + next;
        List<NotificationIntent> notifications = notificationType == null ? new java.util.ArrayList<>()
                : new java.util.ArrayList<>(List.of(
                new NotificationIntent(session.getMemberId(), notificationType, "Care episode update",
                        sessionMessage(notificationType), BusinessDomainType.SESSION, session.getId(), key + ":member"),
                new NotificationIntent(session.getDoctorId(), notificationType, "Care episode update",
                        sessionMessage(notificationType), BusinessDomainType.SESSION, session.getId(), key + ":doctor")));
        if (eventType == BusinessEventType.SESSION_TERMINATION_REQUESTED
                || eventType == BusinessEventType.SESSION_PARTICIPANT_INTERRUPTED)
            notifications.add(NotificationIntent.forRole(UserRole.CARE_COORDINATOR,
                    NotificationType.OPERATIONAL_REVIEW_REQUIRED, "Care episode requires review",
                    "An active care episode requires coordinator review.", BusinessDomainType.SESSION,
                    session.getId(), key + ":coordinators"));
        OperationalEventPublisher.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.SESSION).domainId(session.getId()).eventType(eventType)
                .actorType(actorId == null ? BusinessActorType.SYSTEM : BusinessActorType.USER)
                .actorUserId(actorId).actorRole(actorRole == null ? null : actorRole.name())
                .requestId(session.getRequestId()).sessionId(session.getId()).memberId(session.getMemberId())
                .doctorId(session.getDoctorId()).previousState(previous == null ? null : previous.name())
                .newState(next == null ? null : next.name()).reason(reason).idempotencyKey(key)
                .needsAction(needsAction).notifications(notifications).build());
    }

    private String sessionMessage(NotificationType type) {
        return switch (type) {
            case CARE_ACTIVATED -> "The care episode is now active.";
            case CARE_ENDING -> "The care episode is ending within 24 hours.";
            case CARE_COMPLETED -> "The care period has completed.";
            case CARE_CANCELLED -> "The care episode was administratively closed.";
            case CARE_TERMINATION_REQUESTED -> "A termination request is awaiting operational review.";
            case OPERATIONAL_REVIEW_REQUIRED -> "The active care episode requires operational review.";
            default -> "The care episode status changed.";
        };
    }

    private void validateConsultationManager(UserRole actorRole) {
        if (actorRole == UserRole.SUPER_ADMIN
                || actorRole == UserRole.ADMIN
                || actorRole == UserRole.CARE_COORDINATOR)
            return;
        throw new AppException(ErrorCode.ACCESS_DENIED, "You are not allowed to manage consultation sessions");
    }

    private ConsultationSessionResponse toSessionResponse(ConsultationSession session, Long userId) {
        ConsultationSessionResponse response = mapper.toSessionResponse(session);
        if (session.getDoctorId().equals(userId) && session.getActivatedAt() == null)
            response.setHealthRecordId(null);
        participantRepository.findBySessionIdAndUserId(session.getId(), userId)
                .ifPresent(participant -> response.setUnreadCount(safeCountUnreadMessages(session.getId(), userId, participant.getLastReadAt())));
        return response;
    }

    private ConsultationSessionResponse toSessionResponse(ConsultationSession session) {
        ConsultationSessionResponse response = mapper.toSessionResponse(session);
        if (response != null)
            enrichParticipantDisplayNames(List.of(response));
        return response;
    }

    private PageResponse<ConsultationSessionResponse> toPageResponse(
            Page<ConsultationSessionResponse> page,
            Pageable pageable
    ) {
        List<ConsultationSessionResponse> responses = page.getContent();
        enrichParticipantDisplayNames(responses);
        return new PageResponse<>(new PageImpl<>(responses, pageable, page.getTotalElements()));
    }

    private void enrichParticipantDisplayNames(Collection<ConsultationSessionResponse> responses) {
        Set<Long> userIds = new HashSet<>();
        for (ConsultationSessionResponse response : responses) {
            if (response == null)
                continue;
            if (response.getMemberId() != null)
                userIds.add(response.getMemberId());
            if (response.getDoctorId() != null)
                userIds.add(response.getDoctorId());
        }
        if (userIds.isEmpty())
            return;

        List<UserAccount> users = userAccountRepository.findByIdIn(userIds);
        if (users == null || users.isEmpty())
            return;

        Map<Long, String> displayNamesById = new HashMap<>();
        for (UserAccount user : users) {
            if (user != null)
                displayNamesById.put(user.getId(), displayNameOf(user));
        }
        for (ConsultationSessionResponse response : responses) {
            if (response == null)
                continue;
            response.setMemberDisplayName(displayNamesById.get(response.getMemberId()));
            response.setDoctorDisplayName(displayNamesById.get(response.getDoctorId()));
        }
    }

    private String displayNameOf(UserAccount user) {
        return user.getProfile() == null ? null : user.getProfile().getDisplayName();
    }

    private long safeCountUnreadMessages(Long sessionId, Long userId, Instant lastReadAt) {
        try {
            return countUnreadMessages(sessionId, userId, lastReadAt);
        } catch (RuntimeException exception) {
            log.warn("Could not count unread consultation messages for session {} and user {}: {}",
                    sessionId,
                    userId,
                    exception.getMessage()
            );
            return 0;
        }
    }

    private long countUnreadMessages(Long sessionId, Long userId, Instant lastReadAt) {
        if (lastReadAt == null) {
            return messageRepository.countBySessionIdAndSenderIdNot(sessionId, userId);
        }

        return messageRepository.countBySessionIdAndCreatedAtAfterAndSenderIdNot(sessionId, lastReadAt, userId);
    }

    private void validateMember(Long memberId) {
        UserAccount member = userAccountRepository.findById(memberId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
        if (member.getRole() != UserRole.MEMBER) {
            throw new AppException(ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    private DoctorCareProfile validateDoctorForConsultation(Long doctorId) {
        UserAccount doctor = userAccountRepository.findByIdForUpdate(doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));
        if (doctor.getRole() != UserRole.DOCTOR)
            throw new AppException(ErrorCode.DOCTOR_NOT_FOUND);
        if (doctor.getStatus() != AccountStatus.ACTIVE)
            throw new AppException(ErrorCode.DOCTOR_NOT_ELIGIBLE_FOR_CONSULTATION);

        DoctorCareProfile profile = doctorCareProfileRepository.findByDoctorId(doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_CARE_PROFILE_NOT_FOUND));
        if (!Boolean.TRUE.equals(profile.getAcceptsOneOnOneCare())
                || profile.getSpecialty() == null
                || profile.getMaxActiveConsultations() == null
                || profile.getMaxActiveConsultations() <= 0
                || !scheduleValidator.isValid(profile.getAvailabilityJson(), profile.getTimezone(), true))
            throw new AppException(ErrorCode.DOCTOR_NOT_ELIGIBLE_FOR_CONSULTATION);
        return profile;
    }

    private CareServicePackage findOptionalActivePackage(Long packageId) {
        if (packageId == null)
            return null;
        return packageRepository.findByIdAndStatus(packageId, CareServicePackageStatus.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.CARE_SERVICE_PACKAGE_NOT_FOUND));
    }

    private long getDoctorEffectiveLoad(Long doctorId, Instant now) {
        return reservationService.getEffectiveLoad(doctorId, now);
    }

    private void validateHealthRecordOwner(Long healthRecordId, Long memberId) {
        if (healthRecordId == null)
            return;

        healthRecordRepository.findByIdAndUserId(healthRecordId, memberId)
                .orElseThrow(() -> new AppException(ErrorCode.HEALTH_RECORD_NOT_FOUND));
    }

    private void validateConsultationPeriod(Instant endsAt, Instant supportEndsAt) {
        if (supportEndsAt != null && supportEndsAt.isBefore(endsAt)) {
            throw new AppException(ErrorCode.INVALID_PARAMETER, "Support end time must not be before consultation end time");
        }
    }
}
