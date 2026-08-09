package fit.iuh.se.hschat.service.session.impl;

import fit.iuh.se.hschat.dto.request.AdminCreateConsultationSessionRequest;
import fit.iuh.se.hschat.dto.request.CloseConsultationRequest;
import fit.iuh.se.hschat.dto.request.ExtendConsultationRequest;
import fit.iuh.se.hschat.dto.response.ConsultationSessionResponse;
import fit.iuh.se.hschat.entity.CareServicePackage;
import fit.iuh.se.hschat.entity.ConsultationParticipant;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.DoctorCareProfile;
import fit.iuh.se.hschat.entity.enums.CareServicePackageStatus;
import fit.iuh.se.hschat.entity.enums.ConsultationCompletionReason;
import fit.iuh.se.hschat.entity.enums.ConsultationParticipantRole;
import fit.iuh.se.hschat.entity.enums.ConsultationRequestStatus;
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
import fit.iuh.se.hschat.service.session.ConsultationSessionService;
import fit.iuh.se.hshealthrecord.repository.HealthRecordRepository;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

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
    ConsultationMapper mapper;

    @NonFinal
    @Value("${app.consultation.default-doctor-max-active-sessions:20}")
    int defaultDoctorMaxActiveSessions;

    @Override
    @Transactional
    public ConsultationSessionResponse createSessionByAdmin(Long actorId, UserRole actorRole, AdminCreateConsultationSessionRequest request) {
        validateConsultationManager(actorRole);
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
                .sourceType(ConsultationSourceType.ADMIN_CREATED)
                .status(status)
                .startedAt(startedAt)
                .endsAt(request.getEndsAt())
                .supportEndsAt(supportEndsAt)
                .packageId(carePackage == null ? null : carePackage.getId())
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

        return mapper.toSessionResponse(session);
    }

    @Override
    public ConsultationSessionResponse getSessionById(Long userId, Long sessionId) {
        if (!participantRepository.existsBySessionIdAndUserIdAndActiveTrue(sessionId, userId))
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);

        return sessionRepository.findById(sessionId)
                .map(mapper::toSessionResponse)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_NOT_FOUND));
    }

    @Override
    public PageResponse<ConsultationSessionResponse> getMySessions(Long userId, Pageable pageable) {
        Page<ConsultationSessionResponse> page = sessionRepository
                .findByMemberIdOrderByLastMessageAtDesc(userId, pageable)
                .map(session -> toSessionResponse(session, userId));
        return new PageResponse<>(page);
    }

    @Override
    public PageResponse<ConsultationSessionResponse> getDoctorSessions(Long doctorId, Pageable pageable) {
        Page<ConsultationSessionResponse> page = sessionRepository
                .findByDoctorIdOrderByLastMessageAtDesc(doctorId, pageable)
                .map(session -> toSessionResponse(session, doctorId));
        return new PageResponse<>(page);
    }

    @Override
    public PageResponse<ConsultationSessionResponse> getSessionsForAdmin(UserRole actorRole, Pageable pageable) {
        validateConsultationManager(actorRole);
        Page<ConsultationSessionResponse> page = sessionRepository
                .findAll(pageable)
                .map(mapper::toSessionResponse);
        return new PageResponse<>(page);
    }

    @Override
    public ConsultationSessionResponse extendSession(Long actorId, UserRole actorRole, Long sessionId, ExtendConsultationRequest request) {
        validateConsultationManager(actorRole);
        ConsultationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_NOT_FOUND));

        if (session.getStatus() != ConsultationStatus.ACTIVE)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);

        validateConsultationPeriod(request.getEndsAt(), request.getSupportEndsAt());

        Instant supportEndsAt = request.getSupportEndsAt() == null ? request.getEndsAt() : request.getSupportEndsAt();
        session.setEndsAt(request.getEndsAt());
        session.setSupportEndsAt(supportEndsAt);

        session = sessionRepository.save(session);
        return mapper.toSessionResponse(session);
    }

    @Override
    @Transactional
    public ConsultationSessionResponse closeSession(Long actorId, UserRole actorRole, Long sessionId, CloseConsultationRequest request) {
        validateConsultationManager(actorRole);
        ConsultationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_NOT_FOUND));

        if (session.getStatus() != ConsultationStatus.ACTIVE
                && session.getStatus() != ConsultationStatus.SCHEDULED)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);

        Instant now = Instant.now();
        session.setStatus(ConsultationStatus.CANCELLED);
        session.setClosedAt(now);
        session.setCompletedAt(now);
        session.setCompletionReason(ConsultationCompletionReason.ADMINISTRATIVE_CANCELLATION);
        session.setCloseReason(request.getCloseReason());

        session = sessionRepository.save(session);
        return mapper.toSessionResponse(session);
    }

    @Override
    @Transactional
    public void expireOverdueSessions(UserRole actorRole) {
        validateConsultationManager(actorRole);
        Instant now = Instant.now();
        sessionRepository.findByStatusAndEndsAtBefore(ConsultationStatus.ACTIVE, now)
                .forEach(session -> {
                    session.setStatus(ConsultationStatus.COMPLETED);
                    session.setCompletedAt(now);
                    session.setCompletionReason(ConsultationCompletionReason.PERIOD_ENDED);
                    session.setCloseReason("Consultation session completed automatically because the care period ended");
                    sessionRepository.save(session);
                });
    }

    @Override
    @Transactional
    public void activateScheduledSessions(UserRole actorRole) {
        validateConsultationManager(actorRole);
        Instant now = Instant.now();
        sessionRepository.findByStatusAndStartedAtBefore(ConsultationStatus.SCHEDULED, now)
                .forEach(session -> {
                    session.setStatus(ConsultationStatus.ACTIVE);
                    sessionRepository.save(session);
                });
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
        participantRepository.findBySessionIdAndUserId(session.getId(), userId)
                .ifPresent(participant -> response.setUnreadCount(safeCountUnreadMessages(session.getId(), userId, participant.getLastReadAt())));
        return response;
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
        UserAccount doctor = userAccountRepository.findById(doctorId)
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
        long scheduledOrActiveSessions = sessionRepository.countByDoctorIdAndStatusIn(
                doctorId,
                MEMBER_BUSY_SESSION_STATUSES
        );
        long activeReservations = requestRepository.countByAssignedDoctorIdAndStatusAndPaymentDeadlineAfter(
                doctorId,
                ConsultationRequestStatus.WAITING_PAYMENT,
                now
        );
        return scheduledOrActiveSessions + activeReservations;
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
