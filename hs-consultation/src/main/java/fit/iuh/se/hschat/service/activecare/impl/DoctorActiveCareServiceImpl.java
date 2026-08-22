package fit.iuh.se.hschat.service.activecare.impl;

import fit.iuh.se.hschat.dto.response.*;
import fit.iuh.se.hschat.entity.ConsultationHealthRecordAttention;
import fit.iuh.se.hschat.entity.ConsultationParticipant;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.ConsultationAttentionStatus;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.mapper.ConsultationMapper;
import fit.iuh.se.hschat.repository.ConsultationHealthRecordAttentionRepository;
import fit.iuh.se.hschat.repository.ConsultationMessageRepository;
import fit.iuh.se.hschat.repository.ConsultationParticipantRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.service.activecare.DoctorActiveCareService;
import fit.iuh.se.hshealthrecord.entity.HealthRecord;
import fit.iuh.se.hshealthrecord.mapper.HealthRecordMapper;
import fit.iuh.se.hshealthrecord.repository.HealthRecordRepository;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DoctorActiveCareServiceImpl implements DoctorActiveCareService {

    static final List<ConsultationStatus> DASHBOARD_STATUSES = List.of(
            ConsultationStatus.ACTIVE,
            ConsultationStatus.SCHEDULED,
            ConsultationStatus.COMPLETED
    );

    ConsultationSessionRepository sessionRepository;
    ConsultationParticipantRepository participantRepository;
    ConsultationMessageRepository messageRepository;
    ConsultationHealthRecordAttentionRepository attentionRepository;
    HealthRecordRepository healthRecordRepository;
    UserAccountRepository userAccountRepository;
    ConsultationMapper consultationMapper;
    HealthRecordMapper healthRecordMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DoctorConsultationSessionResponse> getAssignedSessions(Long doctorId, Pageable pageable) {
        Page<DoctorConsultationSessionResponse> page = sessionRepository
                .findByDoctorIdAndStatusInOrderByLastMessageAtDesc(doctorId, DASHBOARD_STATUSES, pageable)
                .map(session -> toDoctorSessionResponse(session, doctorId));
        return new PageResponse<>(page);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorConsultationDetailResponse getSessionDetail(Long doctorId, Long sessionId) {
        ConsultationSession session = getAssignedSession(doctorId, sessionId);
        return DoctorConsultationDetailResponse.builder()
                .session(consultationMapper.toSessionResponse(session))
                .member(toUserSummary(findUser(session.getMemberId())))
                .initialHealthRecord(session.getHealthRecordId() == null ? null : getScopedHealthRecord(doctorId, sessionId, session.getHealthRecordId()))
                .unresolvedAttentionCount(attentionRepository.countBySessionIdAndStatus(sessionId, ConsultationAttentionStatus.REQUIRES_ATTENTION))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DoctorScopedHealthRecordResponse> getScopedHealthRecords(Long doctorId, Long sessionId, Pageable pageable) {
        ConsultationSession session = getAssignedSessionForRecordAccess(doctorId, sessionId);
        List<HealthRecord> records = getScopedRecords(session);
        List<DoctorScopedHealthRecordResponse> responses = records.stream()
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .map(record -> toScopedRecordResponse(session, record))
                .toList();

        Page<DoctorScopedHealthRecordResponse> page = new PageImpl<>(responses, pageable, records.size());
        return new PageResponse<>(page);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorScopedHealthRecordResponse getScopedHealthRecord(Long doctorId, Long sessionId, Long recordId) {
        ConsultationSession session = getAssignedSessionForRecordAccess(doctorId, sessionId);
        HealthRecord record = healthRecordRepository.findByIdAndUserId(recordId, session.getMemberId())
                .orElseThrow(() -> new AppException(ErrorCode.HEALTH_RECORD_NOT_FOUND));
        if (!isRecordInScope(session, record))
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);
        return toScopedRecordResponse(session, record);
    }

    @Override
    @Transactional
    public DoctorScopedHealthRecordResponse markAttentionReviewed(Long doctorId, Long sessionId, Long recordId) {
        ConsultationSession session = getAssignedSessionForRecordAccess(doctorId, sessionId);
        HealthRecord record = healthRecordRepository.findByIdAndUserId(recordId, session.getMemberId())
                .orElseThrow(() -> new AppException(ErrorCode.HEALTH_RECORD_NOT_FOUND));
        if (!isRecordInScope(session, record))
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);

        ConsultationHealthRecordAttention attention = attentionRepository.findBySessionIdAndHealthRecordId(sessionId, recordId)
                .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "Consultation attention not found"));
        attention.setStatus(ConsultationAttentionStatus.REVIEWED);
        attention.setReviewedAt(Instant.now());
        attention.setReviewedByDoctorId(doctorId);
        attentionRepository.save(attention);
        return toScopedRecordResponse(session, record);
    }

    private ConsultationSession getAssignedSession(Long doctorId, Long sessionId) {
        return sessionRepository.findByIdAndDoctorId(sessionId, doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED));
    }

    private ConsultationSession getAssignedSessionForRecordAccess(Long doctorId, Long sessionId) {
        ConsultationSession session = getAssignedSession(doctorId, sessionId);
        if (session.getStatus() != ConsultationStatus.ACTIVE && session.getStatus() != ConsultationStatus.COMPLETED)
            throw new AppException(ErrorCode.CONSULTATION_NOT_ACTIVE);
        return session;
    }

    private List<HealthRecord> getScopedRecords(ConsultationSession session) {
        Instant from = session.getStartedAt() == null ? session.getCreatedAt() : session.getStartedAt();
        Instant to = session.getEndsAt();
        List<HealthRecord> records = healthRecordRepository
                .findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(session.getMemberId(), from, to);

        if (session.getHealthRecordId() != null) {
            healthRecordRepository.findByIdAndUserId(session.getHealthRecordId(), session.getMemberId())
                    .filter(attached -> records.stream().noneMatch(record -> record.getId().equals(attached.getId())))
                    .ifPresent(records::add);
        }

        return records.stream()
                .filter(record -> isRecordInScope(session, record))
                .sorted(Comparator.comparing(HealthRecord::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private boolean isRecordInScope(ConsultationSession session, HealthRecord record) {
        if (!record.getUserId().equals(session.getMemberId()))
            return false;
        if (Objects.equals(record.getId(), session.getHealthRecordId()))
            return true;
        Instant startedAt = session.getStartedAt();
        Instant endsAt = session.getEndsAt();
        Instant createdAt = record.getCreatedAt();
        return startedAt != null
                && endsAt != null
                && createdAt != null
                && !createdAt.isBefore(startedAt)
                && !createdAt.isAfter(endsAt);
    }

    private DoctorConsultationSessionResponse toDoctorSessionResponse(ConsultationSession session, Long doctorId) {
        return DoctorConsultationSessionResponse.builder()
                .id(session.getId())
                .status(session.getStatus())
                .member(toUserSummary(findUser(session.getMemberId())))
                .startedAt(session.getStartedAt())
                .endsAt(session.getEndsAt())
                .supportEndsAt(session.getSupportEndsAt())
                .healthRecordId(session.getHealthRecordId())
                .lastMessagePreview(session.getLastMessagePreview())
                .lastMessageAt(session.getLastMessageAt())
                .unreadCount(countUnread(session, doctorId))
                .unresolvedAttentionCount(attentionRepository.countBySessionIdAndStatus(session.getId(), ConsultationAttentionStatus.REQUIRES_ATTENTION))
                .build();
    }

    private DoctorScopedHealthRecordResponse toScopedRecordResponse(ConsultationSession session, HealthRecord record) {
        return DoctorScopedHealthRecordResponse.builder()
                .record(healthRecordMapper.toResponse(record))
                .initialAttachedRecord(Objects.equals(record.getId(), session.getHealthRecordId()))
                .attention(attentionRepository.findBySessionIdAndHealthRecordId(session.getId(), record.getId())
                        .map(this::toAttentionResponse)
                        .orElse(null))
                .build();
    }

    private ConsultationAttentionResponse toAttentionResponse(ConsultationHealthRecordAttention attention) {
        return ConsultationAttentionResponse.builder()
                .id(attention.getId())
                .sessionId(attention.getSessionId())
                .healthRecordId(attention.getHealthRecordId())
                .status(attention.getStatus())
                .reason(attention.getReason())
                .reviewedAt(attention.getReviewedAt())
                .reviewedByDoctorId(attention.getReviewedByDoctorId())
                .createdAt(attention.getCreatedAt())
                .updatedAt(attention.getUpdatedAt())
                .build();
    }

    private long countUnread(ConsultationSession session, Long doctorId) {
        return participantRepository.findBySessionIdAndUserId(session.getId(), doctorId)
                .map(participant -> countUnreadMessages(session.getId(), doctorId, participant))
                .orElse(0L);
    }

    private long countUnreadMessages(Long sessionId, Long doctorId, ConsultationParticipant participant) {
        if (participant.getLastReadAt() == null)
            return messageRepository.countBySessionIdAndSenderIdNot(sessionId, doctorId);
        return messageRepository.countBySessionIdAndCreatedAtAfterAndSenderIdNot(sessionId, participant.getLastReadAt(), doctorId);
    }

    private UserAccount findUser(Long userId) {
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private UserSummaryResponse toUserSummary(UserAccount user) {
        return UserSummaryResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .displayName(user.getProfile() == null ? null : user.getProfile().getDisplayName())
                .phone(user.getProfile() == null ? null : user.getProfile().getPhone())
                .build();
    }
}
