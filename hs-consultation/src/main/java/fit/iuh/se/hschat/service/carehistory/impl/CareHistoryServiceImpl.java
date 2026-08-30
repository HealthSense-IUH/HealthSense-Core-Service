package fit.iuh.se.hschat.service.carehistory.impl;

import fit.iuh.se.hschat.dto.response.*;
import fit.iuh.se.hschat.entity.ConsultationFinalSummary;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.EpisodeHealthRecordAuthorization;
import fit.iuh.se.hschat.entity.enums.ConsultationFinalSummaryStatus;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.repository.ConsultationFinalSummaryRepository;
import fit.iuh.se.hschat.repository.ConsultationFinalSummaryAddendumRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.service.authorization.EpisodeHealthRecordAuthorizationService;
import fit.iuh.se.hschat.service.carehistory.CareHistoryService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsoperations.dto.command.OperationalEventCommand;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.service.OperationalEventService;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CareHistoryServiceImpl implements CareHistoryService {

    ConsultationSessionRepository sessionRepository;
    ConsultationFinalSummaryRepository summaryRepository;
    ConsultationFinalSummaryAddendumRepository addendumRepository;
    EpisodeHealthRecordAuthorizationService authorizationService;
    OperationalEventService operationalEventService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CareHistoryEpisodeResponse> getMemberHistory(Long memberId, Pageable pageable) {
        Page<CareHistoryEpisodeResponse> page = sessionRepository
                .findByMemberIdAndActivatedAtIsNotNullOrderByStartedAtDesc(memberId, pageable)
                .map(this::toMemberEpisode);
        return new PageResponse<>(page);
    }

    @Override
    @Transactional(readOnly = true)
    public CareHistoryEpisodeResponse getMemberEpisode(Long memberId, Long sessionId) {
        ConsultationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_NOT_FOUND));
        if (!session.getMemberId().equals(memberId))
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);
        if (session.getActivatedAt() == null)
            throw new AppException(ErrorCode.CONSULTATION_NOT_ACTIVE,
                    "Care history exists only for an activated episode");
        return toMemberEpisode(session);
    }

    @Override
    @Transactional
    public List<CareContinuitySummaryResponse> getContinuitySummaries(
            Long doctorId, Long currentSessionId) {
        ConsultationSession current = sessionRepository.findByIdAndDoctorId(currentSessionId, doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED));
        if (current.getStatus() != ConsultationStatus.ACTIVE || current.getActivatedAt() == null)
            throw new AppException(ErrorCode.CONSULTATION_NOT_ACTIVE);

        List<ConsultationSession> previous = sessionRepository
                .findByMemberIdAndIdNotAndActivatedAtIsNotNullOrderByStartedAtDesc(
                        current.getMemberId(), currentSessionId);
        Map<Long, ConsultationFinalSummary> finalized = previous.isEmpty() ? Map.of() : summaryRepository
                .findBySessionIdInAndStatus(
                        previous.stream().map(ConsultationSession::getId).toList(),
                        ConsultationFinalSummaryStatus.FINALIZED)
                .stream()
                .collect(Collectors.toMap(ConsultationFinalSummary::getSessionId, Function.identity()));
        List<CareContinuitySummaryResponse> result = previous.stream()
                .filter(session -> finalized.containsKey(session.getId()))
                .map(session -> CareContinuitySummaryResponse.builder()
                        .sessionId(session.getId())
                        .doctorId(session.getDoctorId())
                        .packageId(session.getPackageId())
                        .packageVersion(session.getPackageVersion())
                        .startedAt(session.getStartedAt())
                        .endsAt(session.getEndsAt())
                        .status(session.getStatus())
                        .finalizedSummary(toSummary(finalized.get(session.getId())))
                        .build())
                .toList();
        Instant bucket = Instant.now().truncatedTo(ChronoUnit.HOURS);
        operationalEventService.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.SESSION).domainId(current.getId())
                .eventType(BusinessEventType.CARE_CONTINUITY_ACCESSED)
                .actorType(BusinessActorType.USER).actorUserId(doctorId).actorRole(UserRole.DOCTOR.name())
                .sessionId(current.getId()).memberId(current.getMemberId()).doctorId(doctorId)
                .metadata(Map.of("accessContext", "ACTIVE_CARE_CONTINUITY", "summaryCount", String.valueOf(result.size())))
                .idempotencyKey("care-continuity:" + doctorId + ":" + current.getId() + ":" + bucket)
                .build());
        return result;
    }

    private CareHistoryEpisodeResponse toMemberEpisode(ConsultationSession session) {
        ConsultationFinalSummaryResponse finalizedSummary = summaryRepository.findBySessionId(session.getId())
                .filter(summary -> summary.getStatus() == ConsultationFinalSummaryStatus.FINALIZED)
                .map(this::toSummary)
                .orElse(null);
        return CareHistoryEpisodeResponse.builder()
                .sessionId(session.getId())
                .doctorId(session.getDoctorId())
                .packageId(session.getPackageId())
                .packageVersion(session.getPackageVersion())
                .startedAt(session.getStartedAt())
                .activatedAt(session.getActivatedAt())
                .endsAt(session.getEndsAt())
                .status(session.getStatus())
                .summaryClosureStatus(session.getSummaryClosureStatus())
                .summaryDueAt(session.getSummaryDueAt())
                .summaryEscalatedAt(session.getSummaryEscalatedAt())
                .summaryEscalationReason(session.getSummaryEscalationReason())
                .finalizedSummary(finalizedSummary)
                .healthRecords(authorizationService.getSessionAuthorizations(session.getId()).stream()
                        .map(this::toAuthorization)
                        .toList())
                .build();
    }

    private EpisodeHealthRecordAuthorizationResponse toAuthorization(
            EpisodeHealthRecordAuthorization authorization) {
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

    private ConsultationFinalSummaryResponse toSummary(ConsultationFinalSummary summary) {
        return ConsultationFinalSummaryResponse.builder()
                .id(summary.getId())
                .sessionId(summary.getSessionId())
                .status(summary.getStatus())
                .summary(summary.getSummary())
                .observations(summary.getObservations())
                .recommendations(summary.getRecommendations())
                .followUpRecommendation(summary.getFollowUpRecommendation())
                .createdByDoctorId(summary.getCreatedByDoctorId())
                .finalizedAt(summary.getFinalizedAt())
                .createdAt(summary.getCreatedAt())
                .updatedAt(summary.getUpdatedAt())
                .referencedHealthRecordIds(java.util.Set.copyOf(summary.getReferencedHealthRecordIds()))
                .addenda(addendumRepository.findBySummaryIdOrderByAuthoredAtAsc(summary.getId()).stream()
                        .map(addendum -> FinalSummaryAddendumResponse.builder()
                                .id(addendum.getId())
                                .summaryId(addendum.getSummaryId())
                                .sessionId(addendum.getSessionId())
                                .authorDoctorId(addendum.getAuthorDoctorId())
                                .reason(addendum.getReason())
                                .content(addendum.getContent())
                                .authoredAt(addendum.getAuthoredAt())
                                .build())
                        .toList())
                .build();
    }
}
