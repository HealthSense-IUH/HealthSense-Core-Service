package fit.iuh.se.hschat.service.activecare.impl;

import fit.iuh.se.hschat.dto.response.*;
import fit.iuh.se.hschat.entity.ConsultationHealthRecordAttention;
import fit.iuh.se.hschat.entity.ConsultationParticipant;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.EpisodeHealthRecordAuthorization;
import fit.iuh.se.hschat.entity.enums.ConsultationAttentionStatus;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.entity.enums.EpisodeHealthRecordAuthorizationSource;
import fit.iuh.se.hschat.mapper.ConsultationMapper;
import fit.iuh.se.hschat.repository.ConsultationHealthRecordAttentionRepository;
import fit.iuh.se.hschat.repository.ConsultationMessageRepository;
import fit.iuh.se.hschat.repository.ConsultationParticipantRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.service.activecare.DoctorActiveCareService;
import fit.iuh.se.hschat.service.authorization.EpisodeHealthRecordAuthorizationService;
import fit.iuh.se.hshealthrecord.entity.HealthRecord;
import fit.iuh.se.hshealthrecord.mapper.HealthRecordMapper;
import fit.iuh.se.hshealthrecord.repository.HealthRecordRepository;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsshared.service.s3.S3Service;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import fit.iuh.se.hsoperations.dto.command.OperationalEventCommand;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.event.OperationalEventPublisher;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DoctorActiveCareServiceImpl implements DoctorActiveCareService {

    static final List<ConsultationStatus> DASHBOARD_STATUSES = List.of(
            ConsultationStatus.ACTIVE,
            ConsultationStatus.SCHEDULED,
            ConsultationStatus.COMPLETED,
            ConsultationStatus.CANCELLED
    );

    ConsultationSessionRepository sessionRepository;
    ConsultationParticipantRepository participantRepository;
    ConsultationMessageRepository messageRepository;
    ConsultationHealthRecordAttentionRepository attentionRepository;
    HealthRecordRepository healthRecordRepository;
    EpisodeHealthRecordAuthorizationService authorizationService;
    UserAccountRepository userAccountRepository;
    ConsultationMapper consultationMapper;
    HealthRecordMapper healthRecordMapper;
    S3Service s3Service;
    OperationalEventPublisher OperationalEventPublisher;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DoctorConsultationSessionResponse> getAssignedSessions(Long doctorId, Pageable pageable) {
        if (userAccountRepository.findById(doctorId)
                .filter(account -> account.getStatus() == AccountStatus.ACTIVE).isEmpty())
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        Page<DoctorConsultationSessionResponse> page = sessionRepository
                .findByDoctorIdAndStatusInOrderByLastMessageAtDesc(doctorId, DASHBOARD_STATUSES, pageable)
                .map(session -> toDoctorSessionResponse(session, doctorId));
        return new PageResponse<>(page);
    }

    @Override
    @Transactional
    public DoctorConsultationDetailResponse getSessionDetail(Long doctorId, Long sessionId) {
        ConsultationSession session = getAssignedSessionForRecordAccess(doctorId, sessionId);
        EpisodeHealthRecordAuthorization initialAuthorization = authorizationService
                .getSessionAuthorizations(sessionId).stream()
                .filter(item -> item.getAuthorizationSource() == EpisodeHealthRecordAuthorizationSource.INITIAL_SHARED)
                .findFirst()
                .orElse(null);
        return DoctorConsultationDetailResponse.builder()
                .session(consultationMapper.toSessionResponse(session))
                .member(toUserSummary(findUser(session.getMemberId())))
                .initialHealthRecord(initialAuthorization == null ? null
                        : getScopedHealthRecord(doctorId, sessionId, initialAuthorization.getHealthRecordId()))
                .unresolvedAttentionCount(attentionRepository.countBySessionIdAndStatus(sessionId, ConsultationAttentionStatus.REQUIRES_ATTENTION))
                .build();
    }

    @Override
    @Transactional
    public PageResponse<DoctorScopedHealthRecordResponse> getScopedHealthRecords(Long doctorId, Long sessionId, Pageable pageable) {
        ConsultationSession session = getAssignedSessionForRecordAccess(doctorId, sessionId);
        List<HealthRecord> records = getScopedRecords(session);
        List<DoctorScopedHealthRecordResponse> responses = records.stream()
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .map(record -> toScopedRecordResponse(
                        session,
                        record,
                        authorizationService.requireDoctorReadAccess(doctorId, session, record.getId())))
                .toList();

        if (session.getStatus() != ConsultationStatus.ACTIVE)
            responses.forEach(response -> auditRecordAccess(doctorId, session, response.getRecord().getId(),
                    BusinessEventType.HEALTH_RECORD_HISTORICAL_ACCESSED));

        Page<DoctorScopedHealthRecordResponse> page = new PageImpl<>(responses, pageable, records.size());
        return new PageResponse<>(page);
    }

    @Override
    @Transactional
    public DoctorScopedHealthRecordResponse getScopedHealthRecord(Long doctorId, Long sessionId, Long recordId) {
        ConsultationSession session = getAssignedSessionForRecordAccess(doctorId, sessionId);
        HealthRecord record = healthRecordRepository.findByIdAndUserId(recordId, session.getMemberId())
                .orElseThrow(() -> new AppException(ErrorCode.HEALTH_RECORD_NOT_FOUND));
        EpisodeHealthRecordAuthorization authorization = authorizationService
                .requireDoctorReadAccess(doctorId, session, recordId);
        if (session.getStatus() != ConsultationStatus.ACTIVE)
            auditRecordAccess(doctorId, session, recordId, BusinessEventType.HEALTH_RECORD_HISTORICAL_ACCESSED);
        return toScopedRecordResponse(session, record, authorization);
    }

    @Override
    @Transactional
    public RawHealthRecordArtifactResponse getRawArtifact(Long doctorId, Long sessionId, Long recordId) {
        ConsultationSession session = getAssignedSessionForRecordAccess(doctorId, sessionId);
        HealthRecord record = healthRecordRepository.findByIdAndUserId(recordId, session.getMemberId())
                .orElseThrow(() -> new AppException(ErrorCode.HEALTH_RECORD_NOT_FOUND));
        authorizationService.requireDoctorReadAccess(doctorId, session, recordId);
        auditRecordAccess(doctorId, session, recordId, BusinessEventType.HEALTH_RECORD_RAW_ARTIFACT_ACCESSED);
        return RawHealthRecordArtifactResponse.builder()
                .sessionId(sessionId)
                .healthRecordId(recordId)
                .fileName(record.getFileName())
                .downloadUrl(s3Service.generatePresignedDownloadUrl(record.getS3FileKey()))
                .build();
    }

    @Override
    @Transactional
    public DoctorScopedHealthRecordResponse markAttentionReviewed(Long doctorId, Long sessionId, Long recordId) {
        ConsultationSession session = getAssignedSessionForRecordAccess(doctorId, sessionId);
        HealthRecord record = healthRecordRepository.findByIdAndUserId(recordId, session.getMemberId())
                .orElseThrow(() -> new AppException(ErrorCode.HEALTH_RECORD_NOT_FOUND));
        EpisodeHealthRecordAuthorization authorization = authorizationService
                .requireDoctorCurrentWriteAccess(doctorId, session, recordId);

        ConsultationHealthRecordAttention attention = attentionRepository.findBySessionIdAndHealthRecordId(sessionId, recordId)
                .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "Consultation attention not found"));
        attention.setStatus(ConsultationAttentionStatus.REVIEWED);
        attention.setReviewedAt(Instant.now());
        attention.setReviewedByDoctorId(doctorId);
        attentionRepository.save(attention);
        return toScopedRecordResponse(session, record, authorization);
    }

    private ConsultationSession getAssignedSession(Long doctorId, Long sessionId) {
        return sessionRepository.findByIdAndDoctorId(sessionId, doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED));
    }

    private void auditRecordAccess(Long doctorId, ConsultationSession session, Long recordId,
            BusinessEventType eventType) {
        Instant bucket = Instant.now().truncatedTo(ChronoUnit.HOURS);
        OperationalEventPublisher.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.HEALTH_RECORD).domainId(recordId).eventType(eventType)
                .actorType(BusinessActorType.USER).actorUserId(doctorId).actorRole(UserRole.DOCTOR.name())
                .sessionId(session.getId()).healthRecordId(recordId).memberId(session.getMemberId()).doctorId(doctorId)
                .metadata(Map.of("accessContext", session.getStatus() == ConsultationStatus.ACTIVE
                        ? "ACTIVE_EPISODE" : "RETAINED_HISTORICAL_EPISODE"))
                .occurredAt(Instant.now()).idempotencyKey("health-record-access:" + eventType + ":" + doctorId
                        + ":" + session.getId() + ":" + recordId + ":" + bucket)
                .build());
    }

    private ConsultationSession getAssignedSessionForRecordAccess(Long doctorId, Long sessionId) {
        ConsultationSession session = getAssignedSession(doctorId, sessionId);
        if (session.getActivatedAt() == null
                || (session.getStatus() != ConsultationStatus.ACTIVE
                && session.getStatus() != ConsultationStatus.COMPLETED
                && session.getStatus() != ConsultationStatus.CANCELLED))
            throw new AppException(ErrorCode.CONSULTATION_NOT_ACTIVE);
        if (session.getStatus() == ConsultationStatus.ACTIVE
                && userAccountRepository.findById(doctorId)
                .filter(account -> account.getStatus() == AccountStatus.ACTIVE).isEmpty())
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        return session;
    }

    private List<HealthRecord> getScopedRecords(ConsultationSession session) {
        List<EpisodeHealthRecordAuthorization> authorizations = authorizationService
                .getSessionAuthorizations(session.getId());
        Map<Long, HealthRecord> records = healthRecordRepository.findAllById(
                        authorizations.stream().map(EpisodeHealthRecordAuthorization::getHealthRecordId).toList())
                .stream()
                .filter(record -> record.getUserId().equals(session.getMemberId()))
                .collect(Collectors.toMap(HealthRecord::getId, Function.identity()));
        return authorizations.stream()
                .map(item -> records.get(item.getHealthRecordId()))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private DoctorConsultationSessionResponse toDoctorSessionResponse(ConsultationSession session, Long doctorId) {
        return DoctorConsultationSessionResponse.builder()
                .id(session.getId())
                .status(session.getStatus())
                .member(toUserSummary(findUser(session.getMemberId())))
                .startedAt(session.getStartedAt())
                .endsAt(session.getEndsAt())
                .supportEndsAt(session.getSupportEndsAt())
                .healthRecordId(session.getActivatedAt() == null ? null : session.getHealthRecordId())
                .lastMessagePreview(session.getLastMessagePreview())
                .lastMessageAt(session.getLastMessageAt())
                .unreadCount(countUnread(session, doctorId))
                .unresolvedAttentionCount(attentionRepository.countBySessionIdAndStatus(session.getId(), ConsultationAttentionStatus.REQUIRES_ATTENTION))
                .build();
    }

    private DoctorScopedHealthRecordResponse toScopedRecordResponse(
            ConsultationSession session,
            HealthRecord record,
            EpisodeHealthRecordAuthorization authorization) {
        return DoctorScopedHealthRecordResponse.builder()
                .record(healthRecordMapper.toResponse(record))
                .initialAttachedRecord(authorization.getAuthorizationSource()
                        == EpisodeHealthRecordAuthorizationSource.INITIAL_SHARED)
                .authorizationSource(authorization.getAuthorizationSource())
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
