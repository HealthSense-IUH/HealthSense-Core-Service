package fit.iuh.se.hschat.service.continuation.impl;

import fit.iuh.se.hschat.dto.response.ContinuationDecisionResponse;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.*;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.service.continuation.ContinuationState;
import fit.iuh.se.hschat.service.continuation.ContinuationStore;
import fit.iuh.se.hschat.service.continuation.QueueSessionCompletionService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsoperations.dto.command.NotificationIntent;
import fit.iuh.se.hsoperations.dto.command.OperationalEventCommand;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.event.OperationalEventPublisher;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class QueueSessionCompletionServiceImpl implements QueueSessionCompletionService {

    ConsultationSessionRepository sessionRepository;
    ContinuationStore continuationStore;
    OperationalEventPublisher operationalEventPublisher;

    @NonFinal
    @Value("${app.consultation.queue-summary.deadline-minutes:10}")
    long summaryDeadlineMinutes = 10;

    @Override
    @Transactional
    public ContinuationDecisionResponse complete(
            Long sessionId, int expectedRound, Instant completedAt, ConsultationCompletionReason reason) {
        ConsultationSession session = sessionRepository.findByIdForUpdate(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_NOT_FOUND));
        if (session.getFlowType() != ConsultationFlowType.QUEUE_DISPATCH_V1)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);
        if (session.getContinuationRound() != expectedRound)
            throw new AppException(ErrorCode.CONTINUATION_ROUND_STALE);

        ContinuationState state;
        try {
            state = continuationStore.find(sessionId, expectedRound).orElse(null);
        } catch (AppException exception) {
            if (exception.getErrorCode() != ErrorCode.DISPATCH_TEMPORARILY_UNAVAILABLE) throw exception;
            state = null;
        }
        if (session.getStatus() == ConsultationStatus.COMPLETED)
            return response(session, state, expectedRound);
        if (session.getStatus() != ConsultationStatus.ACTIVE)
            throw new AppException(ErrorCode.CONSULTATION_NOT_ACTIVE);

        session.setStatus(ConsultationStatus.COMPLETED);
        session.setCompletedAt(completedAt);
        session.setCompletionReason(reason);
        session.setCloseReason(reason == ConsultationCompletionReason.CONTINUATION_STOPPED
                ? "A participant stopped the consultation at the continuation boundary"
                : "The continuation grace period expired without both participants continuing");
        session.setSummaryClosureStatus(FinalSummaryClosureStatus.SUMMARY_PENDING);
        if (session.getSummaryDueAt() == null)
            session.setSummaryDueAt(completedAt.plus(Duration.ofMinutes(summaryDeadlineMinutes)));
        session.setSummaryEscalatedAt(null);
        session.setSummaryEscalationReason(null);
        sessionRepository.save(session);
        log.info("[QUEUE_SESSION_COMPLETED] pid={} sessionId={} round={} reason={} completedAt={} endsAt={} summaryDueAt={}",
                ProcessHandle.current().pid(), sessionId, expectedRound, reason,
                completedAt, session.getEndsAt(), session.getSummaryDueAt());

        recordCompletion(session, expectedRound, completedAt, reason, state);
        releaseAfterCommit(sessionId, expectedRound);
        return response(session, state, expectedRound);
    }

    private void recordCompletion(ConsultationSession session, int round, Instant completedAt,
            ConsultationCompletionReason reason, ContinuationState state) {
        Map<String, String> metadata = new java.util.LinkedHashMap<>();
        metadata.put("round", Integer.toString(round));
        metadata.put("completedAt", completedAt.toString());
        if (state != null) {
            metadata.put("doctorDecision", state.doctorDecision().name());
            metadata.put("memberDecision", state.memberDecision().name());
            metadata.put("graceExpiresAt", state.graceExpiresAt().toString());
        }
        String key = "queue-session:" + session.getId() + ":round:" + round + ":completed";
        operationalEventPublisher.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.SESSION).domainId(session.getId())
                .eventType(BusinessEventType.SESSION_COMPLETED).actorType(BusinessActorType.SYSTEM)
                .sessionId(session.getId()).memberId(session.getMemberId()).doctorId(session.getDoctorId())
                .previousState(ConsultationStatus.ACTIVE.name()).newState(ConsultationStatus.COMPLETED.name())
                .reason(reason.name()).metadata(metadata).occurredAt(completedAt).idempotencyKey(key)
                .notifications(List.of(
                        new NotificationIntent(session.getMemberId(), NotificationType.CARE_COMPLETED,
                                "Consultation completed", "Your consultation session has completed.",
                                BusinessDomainType.SESSION, session.getId(), key + ":member"),
                        new NotificationIntent(session.getDoctorId(), NotificationType.CARE_COMPLETED,
                                "Consultation completed", "Your consultation session has completed.",
                                BusinessDomainType.SESSION, session.getId(), key + ":doctor")))
                .build());
        operationalEventPublisher.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.FINAL_SUMMARY).domainId(session.getId())
                .eventType(BusinessEventType.SUMMARY_PENDING).actorType(BusinessActorType.SYSTEM)
                .sessionId(session.getId()).memberId(session.getMemberId()).doctorId(session.getDoctorId())
                .newState(FinalSummaryClosureStatus.SUMMARY_PENDING.name()).occurredAt(completedAt)
                .idempotencyKey("queue-session:" + session.getId() + ":summary-pending")
                .metadata(Map.of(
                        "completedAt", completedAt.toString(),
                        "summaryDueAt", session.getSummaryDueAt().toString()))
                .notifications(List.of(new NotificationIntent(
                        session.getDoctorId(), NotificationType.SUMMARY_ACTION_REQUIRED,
                        "Final summary required",
                        "Complete the Final Summary before the displayed deadline to finish this consultation lifecycle.",
                        BusinessDomainType.SESSION, session.getId(),
                        "queue-session:" + session.getId() + ":summary-pending:doctor")))
                .build());
    }

    private void releaseAfterCommit(Long sessionId, int round) {
        Runnable release = () -> {
            try { continuationStore.release(sessionId, round); }
            catch (RuntimeException ignored) { /* recovery cleanup is safe on a later tick/TTL */ }
        };
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            release.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { release.run(); }
        });
    }

    private ContinuationDecisionResponse response(
            ConsultationSession session, ContinuationState state, int round) {
        return new ContinuationDecisionResponse(session.getId(), round,
                state == null ? ContinuationDecision.PENDING : state.doctorDecision(),
                state == null ? ContinuationDecision.PENDING : state.memberDecision(),
                state == null ? session.getEndsAt() : state.promptedAt(),
                state == null ? session.getEndsAt().plusSeconds(300) : state.graceExpiresAt(),
                session.getStatus(), session.getBlockStartedAt(), session.getEndsAt(),
                session.getContinuationRound());
    }
}
