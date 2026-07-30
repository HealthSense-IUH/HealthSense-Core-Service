package fit.iuh.se.hschat.service.session.impl;

import fit.iuh.se.hschat.dto.request.AdminCreateConsultationSessionRequest;
import fit.iuh.se.hschat.dto.request.CloseConsultationRequest;
import fit.iuh.se.hschat.dto.request.ExtendConsultationRequest;
import fit.iuh.se.hschat.dto.response.ConsultationSessionResponse;
import fit.iuh.se.hschat.entity.ConsultationParticipant;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.ConsultationParticipantRole;
import fit.iuh.se.hschat.entity.enums.ConsultationSourceType;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.mapper.ConsultationMapper;
import fit.iuh.se.hschat.repository.ConsultationParticipantRepository;
import fit.iuh.se.hschat.repository.ConsultationMessageRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.service.session.ConsultationSessionService;
import fit.iuh.se.hshealthrecord.repository.HealthRecordRepository;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.UserAccount;
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

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConsultationSessionServiceImpl implements ConsultationSessionService {

    ConsultationSessionRepository sessionRepository;
    ConsultationParticipantRepository participantRepository;
    ConsultationMessageRepository messageRepository;
    HealthRecordRepository healthRecordRepository;
    UserAccountRepository userAccountRepository;
    ConsultationMapper mapper;

    @NonFinal
    @Value("${app.consultation.default-doctor-max-active-sessions:20}")
    int defaultDoctorMaxActiveSessions;

    @Override
    @Transactional
    public ConsultationSessionResponse createSessionByAdmin(Long adminId, AdminCreateConsultationSessionRequest request) {
        log.info("Creating consultation session by admin {} for member {} and doctor {}",
                adminId, request.getMemberId(), request.getDoctorId());

        validateMember(request.getMemberId());
        validateDoctor(request.getDoctorId());
        validateHealthRecordOwner(request.getHealthRecordId(), request.getMemberId());
        validateConsultationPeriod(request.getEndsAt(), request.getSupportEndsAt());

        if (sessionRepository.existsByMemberIdAndStatus(request.getMemberId(), ConsultationStatus.ACTIVE))
            throw new AppException(ErrorCode.MEMBER_ALREADY_HAS_ACTIVE_CONSULTATION);

        long activeConsultations = sessionRepository.countByDoctorIdAndStatus(
                request.getDoctorId(),
                ConsultationStatus.ACTIVE
        );
        if (activeConsultations >= defaultDoctorMaxActiveSessions)
            throw new AppException(ErrorCode.DOCTOR_CAPACITY_EXCEEDED);

        Instant now = Instant.now();
        Instant startedAt = request.getStartedAt() == null ? now : request.getStartedAt();
        Instant supportEndsAt = request.getSupportEndsAt() == null ? request.getEndsAt() : request.getSupportEndsAt();

        ConsultationSession session = ConsultationSession.builder()
                .memberId(request.getMemberId())
                .doctorId(request.getDoctorId())
                .createdByAdminId(adminId)
                .sourceType(ConsultationSourceType.ADMIN_CREATED)
                .status(ConsultationStatus.ACTIVE)
                .startedAt(startedAt)
                .endsAt(request.getEndsAt())
                .supportEndsAt(supportEndsAt)
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
    public PageResponse<ConsultationSessionResponse> getSessionsForAdmin(Pageable pageable) {
        Page<ConsultationSessionResponse> page = sessionRepository
                .findAll(pageable)
                .map(mapper::toSessionResponse);
        return new PageResponse<>(page);
    }

    @Override
    public ConsultationSessionResponse extendSession(Long adminId, Long sessionId, ExtendConsultationRequest request) {
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
    public ConsultationSessionResponse closeSession(Long adminId, Long sessionId, CloseConsultationRequest request) {
        ConsultationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_NOT_FOUND));

        if (session.getStatus() != ConsultationStatus.ACTIVE)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);

        session.setStatus(ConsultationStatus.CLOSED);
        session.setClosedAt(Instant.now());
        session.setCloseReason(request.getCloseReason());

        session = sessionRepository.save(session);
        return mapper.toSessionResponse(session);
    }

    @Override
    @Transactional
    public void expireOverdueSessions() {
        Instant now = Instant.now();
        sessionRepository.findByStatusAndEndsAtBefore(ConsultationStatus.ACTIVE, now)
                .forEach(session -> {
                    session.setStatus(ConsultationStatus.EXPIRED);
                    session.setClosedAt(now);
                    session.setCloseReason("Consultation session expired automatically");
                    sessionRepository.save(session);
                });
    }

    private ConsultationSessionResponse toSessionResponse(ConsultationSession session, Long userId) {
        ConsultationSessionResponse response = mapper.toSessionResponse(session);
        participantRepository.findBySessionIdAndUserId(session.getId(), userId)
                .ifPresent(participant -> response.setUnreadCount(countUnreadMessages(session.getId(), userId, participant.getLastReadAt())));
        return response;
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

    private void validateDoctor(Long doctorId) {
        UserAccount doctor = userAccountRepository.findById(doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_NOT_FOUND));
        if (doctor.getRole() != UserRole.DOCTOR) {
            throw new AppException(ErrorCode.DOCTOR_NOT_FOUND);
        }
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
