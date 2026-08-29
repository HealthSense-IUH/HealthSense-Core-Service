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

        return authorizationRepository.save(EpisodeHealthRecordAuthorization.builder()
                .sessionId(session.getId())
                .healthRecordId(healthRecordId)
                .memberId(session.getMemberId())
                .authorizationSource(source)
                .authorizedAt(Instant.now())
                .authorizedBy(authorizedBy)
                .authorizedByType(authorizedByType)
                .build());
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
