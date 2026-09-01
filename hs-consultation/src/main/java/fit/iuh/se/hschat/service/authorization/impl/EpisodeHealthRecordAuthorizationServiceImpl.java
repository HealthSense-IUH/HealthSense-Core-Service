package fit.iuh.se.hschat.service.authorization.impl;

import fit.iuh.se.hschat.dto.response.EpisodeHealthRecordAuthorizationResponse;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.EpisodeHealthRecordAuthorization;
import fit.iuh.se.hschat.entity.enums.*;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.repository.EpisodeHealthRecordAuthorizationRepository;
import fit.iuh.se.hschat.service.authorization.EpisodeHealthRecordAuthorizationService;
import fit.iuh.se.hshealthrecord.repository.HealthRecordRepository;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsoperations.dto.command.*;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.event.OperationalEventPublisher;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EpisodeHealthRecordAuthorizationServiceImpl
        implements EpisodeHealthRecordAuthorizationService {

    EpisodeHealthRecordAuthorizationRepository authorizationRepository;
    ConsultationSessionRepository sessionRepository;
    HealthRecordRepository healthRecordRepository;
    UserAccountRepository userAccountRepository;
    OperationalEventPublisher OperationalEventPublisher;

    @Override
    @Transactional
    public List<EpisodeHealthRecordAuthorization> authorizeInitialRecords(
            ConsultationSession session, Collection<Long> healthRecordIds) {
        requireActivatedSession(session);
        if (healthRecordIds == null)
            return List.of();
        return healthRecordIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(recordId -> authorize(
                        session,
                        recordId,
                        EpisodeHealthRecordAuthorizationSource.INITIAL_SHARED,
                        session.getMemberId(),
                        EpisodeHealthRecordAuthorizedByType.SYSTEM
                ))
                .toList();
    }

    @Override
    @Transactional
    public EpisodeHealthRecordAuthorization authorizeAdminInitialRecord(
            ConsultationSession session, Long healthRecordId, Long adminId) {
        requireActivatedSession(session);
        return authorize(
                session,
                healthRecordId,
                EpisodeHealthRecordAuthorizationSource.INITIAL_SHARED,
                adminId,
                EpisodeHealthRecordAuthorizedByType.ADMIN_OVERRIDE
        );
    }

    @Override
    @Transactional
    public EpisodeHealthRecordAuthorizationResponse shareDuringActiveCare(
            Long memberId, Long sessionId, Long healthRecordId) {
        ConsultationSession session = sessionRepository.findByIdForUpdate(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_NOT_FOUND));
        if (!session.getMemberId().equals(memberId))
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);
        if (userAccountRepository.findById(memberId)
                .filter(account -> account.getStatus() == AccountStatus.ACTIVE).isEmpty())
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        requireActiveSession(session);
        return toResponse(authorize(
                session,
                healthRecordId,
                EpisodeHealthRecordAuthorizationSource.SHARED_DURING_CARE,
                memberId,
                EpisodeHealthRecordAuthorizedByType.MEMBER
        ));
    }

    @Override
    @Transactional
    public void authorizeCreatedRecord(Long memberId, Long healthRecordId) {
        healthRecordRepository.findByIdAndUserId(healthRecordId, memberId)
                .orElseThrow(() -> new AppException(ErrorCode.HEALTH_RECORD_NOT_FOUND));
        sessionRepository.findAllByMemberIdAndStatus(memberId, ConsultationStatus.ACTIVE)
                .stream()
                .filter(session -> session.getActivatedAt() != null)
                .forEach(session -> sessionRepository.findByIdForUpdate(session.getId())
                        .filter(locked -> locked.getStatus() == ConsultationStatus.ACTIVE
                                && locked.getActivatedAt() != null)
                        .ifPresent(locked -> authorize(
                                locked,
                                healthRecordId,
                                EpisodeHealthRecordAuthorizationSource.CREATED_DURING_CARE,
                                memberId,
                                EpisodeHealthRecordAuthorizedByType.SYSTEM
                        )));
    }

    @Override
    @Transactional(readOnly = true)
    public EpisodeHealthRecordAuthorization requireDoctorReadAccess(
            Long doctorId, ConsultationSession session, Long healthRecordId) {
        requireOwnActivatedEpisode(doctorId, session);
        return authorizationRepository.findBySessionIdAndHealthRecordId(session.getId(), healthRecordId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED));
    }

    @Override
    @Transactional(readOnly = true)
    public EpisodeHealthRecordAuthorization requireDoctorCurrentWriteAccess(
            Long doctorId, ConsultationSession session, Long healthRecordId) {
        requireOwnActivatedEpisode(doctorId, session);
        if (session.getStatus() != ConsultationStatus.ACTIVE)
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED,
                    "Historical episode HealthRecord access is read-only");
        return authorizationRepository.findBySessionIdAndHealthRecordId(session.getId(), healthRecordId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EpisodeHealthRecordAuthorization> getSessionAuthorizations(Long sessionId) {
        return authorizationRepository.findBySessionIdOrderByAuthorizedAtDesc(sessionId);
    }

    private EpisodeHealthRecordAuthorization authorize(
            ConsultationSession session,
            Long healthRecordId,
            EpisodeHealthRecordAuthorizationSource source,
            Long authorizedBy,
            EpisodeHealthRecordAuthorizedByType authorizedByType) {
        healthRecordRepository.findByIdAndUserId(healthRecordId, session.getMemberId())
                .orElseThrow(() -> new AppException(ErrorCode.HEALTH_RECORD_NOT_FOUND));
        EpisodeHealthRecordAuthorization existing = authorizationRepository
                .findBySessionIdAndHealthRecordId(session.getId(), healthRecordId)
                .orElse(null);
        if (existing != null)
            return existing;

        EpisodeHealthRecordAuthorization authorization = authorizationRepository.save(EpisodeHealthRecordAuthorization.builder()
                .sessionId(session.getId())
                .healthRecordId(healthRecordId)
                .memberId(session.getMemberId())
                .authorizationSource(source)
                .authorizedAt(Instant.now())
                .authorizedBy(authorizedBy)
                .authorizedByType(authorizedByType)
                .build());
        BusinessActorType actorType = authorizedByType == EpisodeHealthRecordAuthorizedByType.SYSTEM
                ? BusinessActorType.SYSTEM : BusinessActorType.USER;
        OperationalEventPublisher.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.HEALTH_RECORD).domainId(healthRecordId)
                .eventType(BusinessEventType.HEALTH_RECORD_AUTHORIZED).actorType(actorType)
                .actorUserId(actorType == BusinessActorType.USER ? authorizedBy : null)
                .actorRole(authorizedByType == EpisodeHealthRecordAuthorizedByType.MEMBER ? UserRole.MEMBER.name()
                        : authorizedByType == EpisodeHealthRecordAuthorizedByType.ADMIN_OVERRIDE ? UserRole.ADMIN.name() : null)
                .sessionId(session.getId()).healthRecordId(healthRecordId).memberId(session.getMemberId())
                .doctorId(session.getDoctorId()).newState(source.name())
                .metadata(java.util.Map.of("authorizationSource", source.name()))
                .idempotencyKey("health-record-authorization:" + session.getId() + ":" + healthRecordId)
                .notifications(List.of(new NotificationIntent(session.getDoctorId(), NotificationType.HEALTH_RECORD_AUTHORIZED,
                        "Health record shared", "A HealthRecord was authorized for this active care episode.",
                        BusinessDomainType.HEALTH_RECORD, healthRecordId,
                        "health-record-authorization:" + session.getId() + ":" + healthRecordId + ":doctor")))
                .build());
        return authorization;
    }

    private void requireActiveSession(ConsultationSession session) {
        requireActivatedSession(session);
        if (session.getStatus() != ConsultationStatus.ACTIVE)
            throw new AppException(ErrorCode.CONSULTATION_NOT_ACTIVE);
    }

    private void requireActivatedSession(ConsultationSession session) {
        if (session.getActivatedAt() == null)
            throw new AppException(ErrorCode.CONSULTATION_NOT_ACTIVE);
    }

    private void requireOwnActivatedEpisode(Long doctorId, ConsultationSession session) {
        if (!session.getDoctorId().equals(doctorId) || session.getActivatedAt() == null)
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);
        if (session.getStatus() != ConsultationStatus.ACTIVE
                && session.getStatus() != ConsultationStatus.COMPLETED
                && session.getStatus() != ConsultationStatus.CANCELLED)
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);
    }

    private EpisodeHealthRecordAuthorizationResponse toResponse(EpisodeHealthRecordAuthorization authorization) {
        return EpisodeHealthRecordAuthorizationResponse.builder()
                .id(authorization.getId())
                .sessionId(authorization.getSessionId())
                .healthRecordId(authorization.getHealthRecordId())
                .source(authorization.getAuthorizationSource())
                .authorizedAt(authorization.getAuthorizedAt())
                .authorizedBy(authorization.getAuthorizedBy())
                .authorizedByType(authorization.getAuthorizedByType())
                .build();
    }
}
