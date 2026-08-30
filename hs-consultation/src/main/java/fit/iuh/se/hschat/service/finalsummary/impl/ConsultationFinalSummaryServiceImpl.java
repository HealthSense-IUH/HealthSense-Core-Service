package fit.iuh.se.hschat.service.finalsummary.impl;

import fit.iuh.se.hschat.dto.request.CreateFinalSummaryAddendumRequest;
import fit.iuh.se.hschat.dto.request.UpsertConsultationFinalSummaryRequest;
import fit.iuh.se.hschat.dto.response.ConsultationFinalSummaryResponse;
import fit.iuh.se.hschat.dto.response.FinalSummaryAddendumResponse;
import fit.iuh.se.hschat.entity.ConsultationFinalSummary;
import fit.iuh.se.hschat.entity.ConsultationFinalSummaryAddendum;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.ConsultationFinalSummaryStatus;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.repository.ConsultationFinalSummaryAddendumRepository;
import fit.iuh.se.hschat.repository.ConsultationFinalSummaryRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.repository.EpisodeHealthRecordAuthorizationRepository;
import fit.iuh.se.hschat.service.finalsummary.ConsultationFinalSummaryService;
import fit.iuh.se.hschat.service.finalsummary.FinalSummaryClosureService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import fit.iuh.se.hsoperations.dto.command.*;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.service.OperationalEventService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConsultationFinalSummaryServiceImpl implements ConsultationFinalSummaryService {

    ConsultationSessionRepository sessionRepository;
    ConsultationFinalSummaryRepository summaryRepository;
    ConsultationFinalSummaryAddendumRepository addendumRepository;
    EpisodeHealthRecordAuthorizationRepository authorizationRepository;
    UserAccountRepository userAccountRepository;
    FinalSummaryClosureService closureService;
    OperationalEventService operationalEventService;

    @Override
    @Transactional(readOnly = true)
    public ConsultationFinalSummaryResponse getForDoctor(Long doctorId, Long sessionId) {
        ConsultationSession session = getAssignedDoctorSession(doctorId, sessionId);
        return summaryRepository.findBySessionId(sessionId)
                .map(summary -> toResponse(summary, session))
                .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND, "Final care summary not found"));
    }

    @Override
    @Transactional
    public ConsultationFinalSummaryResponse upsertDraft(
            Long doctorId, Long sessionId, UpsertConsultationFinalSummaryRequest request) {
        requireActiveDoctor(doctorId);
        ConsultationSession session = getAssignedDoctorSession(doctorId, sessionId);
        if (session.getStatus() != ConsultationStatus.ACTIVE
                && session.getStatus() != ConsultationStatus.COMPLETED
                && !(session.getStatus() == ConsultationStatus.CANCELLED
                && Boolean.TRUE.equals(session.getMeaningfulCareOccurred())))
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS,
                    "Final summary draft can be edited only for active or completed sessions");

        ConsultationFinalSummary summary = summaryRepository.findBySessionIdForUpdate(sessionId)
                .orElseGet(() -> ConsultationFinalSummary.builder()
                        .sessionId(sessionId)
                        .createdByDoctorId(doctorId)
                        .status(ConsultationFinalSummaryStatus.DRAFT)
                        .build());

        if (summary.getStatus() == ConsultationFinalSummaryStatus.FINALIZED)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS,
                    "Finalized care summary cannot be edited");
        if (!summary.getCreatedByDoctorId().equals(doctorId))
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);

        apply(summary, request);
        summary.setStatus(ConsultationFinalSummaryStatus.DRAFT);
        summary = summaryRepository.save(summary);
        auditSummary(summary, session, BusinessEventType.FINAL_SUMMARY_DRAFTED, doctorId, null);
        return toResponse(summary, session);
    }

    @Override
    @Transactional
    public ConsultationFinalSummaryResponse finalizeSummary(Long doctorId, Long sessionId) {
        requireActiveDoctor(doctorId);
        ConsultationSession session = getAssignedDoctorSession(doctorId, sessionId);
        if (session.getStatus() != ConsultationStatus.COMPLETED
                && !(session.getStatus() == ConsultationStatus.CANCELLED
                && Boolean.TRUE.equals(session.getMeaningfulCareOccurred())))
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS,
                    "Final summary can be finalized only after session completion");

        ConsultationFinalSummary summary = summaryRepository.findBySessionIdForUpdate(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND,
                        "Final care summary draft not found"));
        if (!summary.getCreatedByDoctorId().equals(doctorId))
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);
        if (summary.getStatus() == ConsultationFinalSummaryStatus.FINALIZED)
            return toResponse(summary, session);

        validateRequiredFinalizationFields(summary);
        Instant now = Instant.now();
        summary.setStatus(ConsultationFinalSummaryStatus.FINALIZED);
        summary.setFinalizedAt(now);
        summary = summaryRepository.save(summary);
        closureService.onSummaryFinalized(session, now);
        auditSummary(summary, session, BusinessEventType.FINAL_SUMMARY_FINALIZED, doctorId,
                new NotificationIntent(session.getMemberId(), NotificationType.FINAL_SUMMARY_AVAILABLE,
                        "Final care summary available", "Your finalized care summary is available in Care History.",
                        BusinessDomainType.FINAL_SUMMARY, summary.getId(),
                        "summary:" + summary.getId() + ":finalized:member"));
        return toResponse(summary, session);
    }

    @Override
    @Transactional
    public FinalSummaryAddendumResponse createAddendum(
            Long doctorId, Long sessionId, CreateFinalSummaryAddendumRequest request) {
        requireActiveDoctor(doctorId);
        requireNonBlank(request.getReason(), "reason");
        requireNonBlank(request.getContent(), "content");
        ConsultationSession session = getAssignedDoctorSession(doctorId, sessionId);
        ConsultationFinalSummary summary = summaryRepository.findBySessionIdForUpdate(sessionId)
                .filter(item -> item.getStatus() == ConsultationFinalSummaryStatus.FINALIZED)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CONSULTATION_STATUS,
                        "An addendum requires a finalized Final Care Summary"));
        if (!summary.getCreatedByDoctorId().equals(doctorId))
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);

        ConsultationFinalSummaryAddendum addendum = addendumRepository.save(
                ConsultationFinalSummaryAddendum.builder()
                        .summaryId(summary.getId())
                        .sessionId(session.getId())
                        .authorDoctorId(doctorId)
                        .reason(request.getReason().trim())
                        .content(request.getContent().trim())
                        .authoredAt(Instant.now())
                        .build());
        operationalEventService.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.FINAL_SUMMARY).domainId(summary.getId())
                .eventType(BusinessEventType.FINAL_SUMMARY_ADDENDUM_CREATED).actorType(BusinessActorType.USER)
                .actorUserId(doctorId).actorRole(UserRole.DOCTOR.name()).sessionId(session.getId())
                .summaryId(summary.getId()).memberId(session.getMemberId()).doctorId(doctorId)
                .reason(request.getReason()).idempotencyKey("summary-addendum:" + addendum.getId() + ":created")
                .build());
        return toAddendumResponse(addendum);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultationFinalSummaryResponse getForMember(Long memberId, Long sessionId) {
        ConsultationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_NOT_FOUND));
        if (!session.getMemberId().equals(memberId))
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);
        return getFinalizedSummary(session);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultationFinalSummaryResponse getForAdmin(UserRole actorRole, Long sessionId) {
        validateConsultationManager(actorRole);
        ConsultationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_NOT_FOUND));
        return getFinalizedSummary(session);
    }

    private void requireActiveDoctor(Long doctorId) {
        boolean activeDoctor = userAccountRepository.findById(doctorId)
                .filter(user -> user.getRole() == UserRole.DOCTOR)
                .filter(user -> user.getStatus() == AccountStatus.ACTIVE)
                .isPresent();
        if (!activeDoctor)
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED,
                    "Only the active assigned Doctor may author clinical closure content");
    }

    private void auditSummary(ConsultationFinalSummary summary, ConsultationSession session,
            BusinessEventType eventType, Long doctorId, NotificationIntent notification) {
        operationalEventService.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.FINAL_SUMMARY).domainId(summary.getId()).eventType(eventType)
                .actorType(BusinessActorType.USER).actorUserId(doctorId).actorRole(UserRole.DOCTOR.name())
                .sessionId(session.getId()).summaryId(summary.getId()).memberId(session.getMemberId()).doctorId(doctorId)
                .newState(summary.getStatus().name()).occurredAt(summary.getFinalizedAt())
                .idempotencyKey("summary:" + summary.getId() + ":" + eventType)
                .notifications(notification == null ? List.of() : List.of(notification)).build());
    }

    private ConsultationSession getAssignedDoctorSession(Long doctorId, Long sessionId) {
        return sessionRepository.findByIdAndDoctorId(sessionId, doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED));
    }

    private ConsultationFinalSummaryResponse getFinalizedSummary(ConsultationSession session) {
        ConsultationFinalSummary summary = summaryRepository.findBySessionId(session.getId())
                .filter(item -> item.getStatus() == ConsultationFinalSummaryStatus.FINALIZED)
                .orElseThrow(() -> new AppException(ErrorCode.ENTITY_NOT_FOUND,
                        "Final care summary has not been finalized"));
        return toResponse(summary, session);
    }

    private void validateConsultationManager(UserRole actorRole) {
        if (actorRole == UserRole.SUPER_ADMIN
                || actorRole == UserRole.ADMIN
                || actorRole == UserRole.CARE_COORDINATOR)
            return;
        throw new AppException(ErrorCode.ACCESS_DENIED,
                "You are not allowed to view final care summaries");
    }

    private void validateRequiredFinalizationFields(ConsultationFinalSummary summary) {
        requireNonBlank(summary.getSummary(), "summary");
        requireNonBlank(summary.getObservations(), "observations");
        requireNonBlank(summary.getRecommendations(), "recommendations");
    }

    private void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank())
            throw new AppException(ErrorCode.INVALID_PARAMETER,
                    field + " is required to finalize a Final Care Summary");
    }

    private void apply(ConsultationFinalSummary summary, UpsertConsultationFinalSummaryRequest request) {
        summary.setSummary(request.getSummary());
        summary.setObservations(request.getObservations());
        summary.setRecommendations(request.getRecommendations());
        summary.setFollowUpRecommendation(emptyToNull(request.getFollowUpRecommendation()));
        if (request.getReferencedHealthRecordIds() != null) {
            Set<Long> references = new LinkedHashSet<>(request.getReferencedHealthRecordIds());
            references.forEach(recordId -> {
                if (!authorizationRepository.existsBySessionIdAndHealthRecordId(summary.getSessionId(), recordId))
                    throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED,
                            "Final Summary may reference only HealthRecords authorized for this episode");
            });
            summary.setReferencedHealthRecordIds(references);
        }
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private ConsultationFinalSummaryResponse toResponse(
            ConsultationFinalSummary summary, ConsultationSession session) {
        List<FinalSummaryAddendumResponse> addenda = summary.getId() == null
                ? List.of()
                : addendumRepository.findBySummaryIdOrderByAuthoredAtAsc(summary.getId()).stream()
                        .map(this::toAddendumResponse)
                        .toList();
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
                .closureStatus(session.getSummaryClosureStatus())
                .summaryDueAt(session.getSummaryDueAt())
                .referencedHealthRecordIds(Set.copyOf(summary.getReferencedHealthRecordIds()))
                .addenda(addenda)
                .build();
    }

    private FinalSummaryAddendumResponse toAddendumResponse(
            ConsultationFinalSummaryAddendum addendum) {
        return FinalSummaryAddendumResponse.builder()
                .id(addendum.getId())
                .summaryId(addendum.getSummaryId())
                .sessionId(addendum.getSessionId())
                .authorDoctorId(addendum.getAuthorDoctorId())
                .reason(addendum.getReason())
                .content(addendum.getContent())
                .authoredAt(addendum.getAuthoredAt())
                .build();
    }
}
