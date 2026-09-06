package fit.iuh.se.hschat.service.continuation.impl;

import fit.iuh.se.hschat.dto.request.SubmitContinuationDecisionRequest;
import fit.iuh.se.hschat.dto.response.ContinuationDecisionResponse;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.*;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.service.continuation.*;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsoperations.dto.command.NotificationIntent;
import fit.iuh.se.hsoperations.dto.command.OperationalEventCommand;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.event.OperationalEventPublisher;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class QueueContinuationServiceImpl implements QueueContinuationService {

    ConsultationSessionRepository sessionRepository;
    ContinuationStore continuationStore;
    QueueSessionCompletionService completionService;
    OperationalEventPublisher operationalEventPublisher;

    @NonFinal
    @Value("${app.consultation.continuation.block-minutes:15}")
    long blockMinutes = 15;

    @NonFinal
    @Value("${app.consultation.continuation.grace-minutes:5}")
    long graceMinutes = 5;

    @NonFinal
    Clock clock = Clock.systemUTC();

    @Override
    @Transactional
    public ContinuationDecisionResponse getCurrent(Long actorId, UserRole actorRole, Long sessionId) {
        ConsultationSession session = lockAndAuthorize(actorId, actorRole, sessionId);
        if (session.getStatus() != ConsultationStatus.ACTIVE) return null;
        Instant now = Instant.now(clock);
        if (now.isBefore(session.getEndsAt())) return null;
        Instant graceExpiresAt = graceExpiresAt(session);
        if (!now.isBefore(graceExpiresAt)) return null;
        ContinuationState state = openIfNecessary(session);
        return response(session, state);
    }

    @Override
    @Transactional
    public ContinuationDecisionResponse decide(Long actorId, UserRole actorRole, Long sessionId,
            int round, SubmitContinuationDecisionRequest request) {
        validateDecision(request.decision());
        ConsultationSession session = lockAndAuthorize(actorId, actorRole, sessionId);
        if (session.getContinuationRound() != round)
            throw new AppException(ErrorCode.CONTINUATION_ROUND_STALE);
        if (session.getStatus() == ConsultationStatus.COMPLETED) {
            if (session.getCompletionReason() == ConsultationCompletionReason.CONTINUATION_GRACE_EXPIRED)
                throw new AppException(ErrorCode.CONTINUATION_EXPIRED);
            if (session.getCompletionReason() == ConsultationCompletionReason.CONTINUATION_STOPPED
                    && request.decision() == ContinuationDecision.STOP)
                return completedIdempotentResponse(session, actorRole, round);
        }
        if (session.getStatus() != ConsultationStatus.ACTIVE)
            throw new AppException(ErrorCode.CONSULTATION_NOT_ACTIVE);

        Instant now = Instant.now(clock);
        if (now.isBefore(session.getEndsAt()))
            throw new AppException(ErrorCode.CONTINUATION_NOT_OPEN);
        if (!now.isBefore(graceExpiresAt(session)))
            throw new AppException(ErrorCode.CONTINUATION_EXPIRED);

        ContinuationState opened = openIfNecessary(session);
        log.info("[QUEUE_CONTINUATION_DECISION_REQUEST] pid={} sessionId={} round={} actorId={} actorRole={} decision={} now={} endsAt={} graceExpiresAt={}",
                ProcessHandle.current().pid(), sessionId, round, actorId, actorRole, request.decision(),
                now, session.getEndsAt(), graceExpiresAt(session));
        ContinuationStore.DecisionOutcome outcome = continuationStore.decide(
                sessionId, round, actorRole, request.decision(), now);
        if (outcome.result() == ContinuationStore.DecisionResult.CONFLICT)
            throw new AppException(ErrorCode.CONTINUATION_DECISION_CONFLICT);
        if (outcome.result() == ContinuationStore.DecisionResult.EXPIRED)
            throw new AppException(ErrorCode.CONTINUATION_EXPIRED);
        if (outcome.result() == ContinuationStore.DecisionResult.STALE || outcome.state() == null)
            throw new AppException(ErrorCode.CONTINUATION_ROUND_STALE);

        ContinuationState state = outcome.state();
        log.info("[QUEUE_CONTINUATION_DECISION_RESULT] pid={} sessionId={} round={} result={} doctorDecision={} memberDecision={}",
                ProcessHandle.current().pid(), sessionId, round, outcome.result(),
                state.doctorDecision(), state.memberDecision());
        if (outcome.result() == ContinuationStore.DecisionResult.ACCEPTED)
            recordDecision(session, actorId, actorRole, request.decision(), state, now);

        if (request.decision() == ContinuationDecision.STOP)
            return completionService.complete(sessionId, round, now,
                    ConsultationCompletionReason.CONTINUATION_STOPPED);

        if (state.bothContinue()) {
            session.setBlockStartedAt(now);
            session.setEndsAt(now.plus(Duration.ofMinutes(blockMinutes)));
            session.setSupportEndsAt(session.getEndsAt());
            session.setContinuationRound(round + 1);
            sessionRepository.save(session);
            log.info("[QUEUE_CONTINUATION_EXTENDED] pid={} sessionId={} previousRound={} nextRound={} blockStartedAt={} endsAt={}",
                    ProcessHandle.current().pid(), sessionId, round, session.getContinuationRound(),
                    session.getBlockStartedAt(), session.getEndsAt());
            recordExtension(session, state, now, round);
            releaseAfterCommit(sessionId, round);
        }
        return response(session, state);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findDueSessionIds(Instant now, int limit) {
        return sessionRepository.findByFlowTypeAndStatusAndEndsAtLessThanEqualOrderByEndsAtAsc(
                        ConsultationFlowType.QUEUE_DISPATCH_V1, ConsultationStatus.ACTIVE, now,
                        PageRequest.of(0, limit))
                .stream().map(ConsultationSession::getId).toList();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processDeadline(Long sessionId, Instant now) {
        ConsultationSession session = sessionRepository.findByIdForUpdate(sessionId).orElse(null);
        if (session == null || session.getFlowType() != ConsultationFlowType.QUEUE_DISPATCH_V1
                || session.getStatus() != ConsultationStatus.ACTIVE || now.isBefore(session.getEndsAt())) return;
        int round = session.getContinuationRound();
        if (!now.isBefore(graceExpiresAt(session))) {
            log.info("[QUEUE_CONTINUATION_GRACE_EXPIRED] pid={} sessionId={} round={} now={} endsAt={} graceExpiresAt={}",
                    ProcessHandle.current().pid(), sessionId, round, now,
                    session.getEndsAt(), graceExpiresAt(session));
            completionService.complete(sessionId, round, now,
                    ConsultationCompletionReason.CONTINUATION_GRACE_EXPIRED);
            return;
        }
        openIfNecessary(session);
    }

    private ContinuationState openIfNecessary(ConsultationSession session) {
        int round = session.getContinuationRound();
        Instant promptedAt = session.getEndsAt();
        Instant graceExpiresAt = graceExpiresAt(session);
        ContinuationStore.OpenResult result = continuationStore.open(
                session.getId(), round, promptedAt, graceExpiresAt);
        ContinuationState state = continuationStore.find(session.getId(), round)
                .orElseThrow(() -> new AppException(ErrorCode.DISPATCH_TEMPORARILY_UNAVAILABLE));
        if (result == ContinuationStore.OpenResult.CREATED) recordPrompt(session, state);
        if (result == ContinuationStore.OpenResult.CREATED) {
            log.info("[QUEUE_CONTINUATION_OPENED] pid={} sessionId={} round={} promptedAt={} graceExpiresAt={}",
                    ProcessHandle.current().pid(), session.getId(), round,
                    state.promptedAt(), state.graceExpiresAt());
        }
        return state;
    }

    private ConsultationSession lockAndAuthorize(Long actorId, UserRole actorRole, Long sessionId) {
        if (actorRole != UserRole.MEMBER && actorRole != UserRole.DOCTOR)
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);
        ConsultationSession session = sessionRepository.findByIdForUpdate(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_NOT_FOUND));
        boolean participant = actorRole == UserRole.MEMBER && actorId.equals(session.getMemberId())
                || actorRole == UserRole.DOCTOR && actorId.equals(session.getDoctorId());
        if (!participant) throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);
        if (session.getFlowType() != ConsultationFlowType.QUEUE_DISPATCH_V1)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS);
        return session;
    }

    private void validateDecision(ContinuationDecision decision) {
        if (decision != ContinuationDecision.CONTINUE && decision != ContinuationDecision.STOP)
            throw new AppException(ErrorCode.INVALID_PARAMETER, "Decision must be CONTINUE or STOP");
    }

    private Instant graceExpiresAt(ConsultationSession session) {
        return session.getEndsAt().plus(Duration.ofMinutes(graceMinutes));
    }

    private ContinuationDecisionResponse response(ConsultationSession session, ContinuationState state) {
        return new ContinuationDecisionResponse(session.getId(), state.round(), state.doctorDecision(),
                state.memberDecision(), state.promptedAt(), state.graceExpiresAt(), session.getStatus(),
                session.getBlockStartedAt(), session.getEndsAt(), session.getContinuationRound());
    }

    private ContinuationDecisionResponse completedIdempotentResponse(
            ConsultationSession session, UserRole actorRole, int round) {
        ContinuationDecision doctor = actorRole == UserRole.DOCTOR
                ? ContinuationDecision.STOP : ContinuationDecision.PENDING;
        ContinuationDecision member = actorRole == UserRole.MEMBER
                ? ContinuationDecision.STOP : ContinuationDecision.PENDING;
        return new ContinuationDecisionResponse(session.getId(), round, doctor, member,
                session.getEndsAt(), graceExpiresAt(session), session.getStatus(),
                session.getBlockStartedAt(), session.getEndsAt(), session.getContinuationRound());
    }

    private void recordPrompt(ConsultationSession session, ContinuationState state) {
        String key = lifecycleKey(session.getId(), state.round(), "requested");
        operationalEventPublisher.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.SESSION).domainId(session.getId())
                .eventType(BusinessEventType.SESSION_CONTINUATION_REQUESTED)
                .actorType(BusinessActorType.SYSTEM).sessionId(session.getId())
                .memberId(session.getMemberId()).doctorId(session.getDoctorId())
                .metadata(timingMetadata(state)).occurredAt(state.promptedAt()).idempotencyKey(key)
                .notifications(List.of(
                        new NotificationIntent(session.getMemberId(), NotificationType.CONTINUATION_CONFIRMATION_REQUIRED,
                                "Continue consultation?", "Choose whether to continue this consultation.",
                                BusinessDomainType.SESSION, session.getId(), key + ":member"),
                        new NotificationIntent(session.getDoctorId(), NotificationType.CONTINUATION_CONFIRMATION_REQUIRED,
                                "Continue consultation?", "Choose whether to continue this consultation.",
                                BusinessDomainType.SESSION, session.getId(), key + ":doctor")))
                .build());
    }

    private void recordDecision(ConsultationSession session, Long actorId, UserRole actorRole,
            ContinuationDecision decision, ContinuationState state, Instant now) {
        Map<String, String> metadata = timingMetadata(state);
        metadata.put("decision", decision.name());
        operationalEventPublisher.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.SESSION).domainId(session.getId())
                .eventType(BusinessEventType.SESSION_CONTINUATION_DECIDED)
                .actorType(BusinessActorType.USER).actorUserId(actorId).actorRole(actorRole.name())
                .sessionId(session.getId()).memberId(session.getMemberId()).doctorId(session.getDoctorId())
                .newState(decision.name()).metadata(metadata).occurredAt(now)
                .idempotencyKey(lifecycleKey(session.getId(), state.round(), "decision:" + actorRole.name()))
                .notifications(List.of()).build());
    }

    private void recordExtension(ConsultationSession session, ContinuationState state, Instant now, int oldRound) {
        Map<String, String> metadata = timingMetadata(state);
        metadata.put("blockStartedAt", session.getBlockStartedAt().toString());
        metadata.put("endsAt", session.getEndsAt().toString());
        metadata.put("nextRound", session.getContinuationRound().toString());
        operationalEventPublisher.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.SESSION).domainId(session.getId())
                .eventType(BusinessEventType.SESSION_BLOCK_EXTENDED).actorType(BusinessActorType.SYSTEM)
                .sessionId(session.getId()).memberId(session.getMemberId()).doctorId(session.getDoctorId())
                .previousState(Integer.toString(oldRound)).newState(session.getContinuationRound().toString())
                .metadata(metadata).occurredAt(now)
                .idempotencyKey(lifecycleKey(session.getId(), oldRound, "extended"))
                .notifications(List.of()).build());
    }

    private Map<String, String> timingMetadata(ContinuationState state) {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("round", Integer.toString(state.round()));
        metadata.put("promptedAt", state.promptedAt().toString());
        metadata.put("graceExpiresAt", state.graceExpiresAt().toString());
        return metadata;
    }

    private String lifecycleKey(Long sessionId, int round, String suffix) {
        return "queue-session:" + sessionId + ":continuation:" + round + ":" + suffix;
    }

    private void releaseAfterCommit(Long sessionId, int round) {
        Runnable release = () -> {
            try { continuationStore.release(sessionId, round); }
            catch (RuntimeException ignored) { /* keyed TTL provides cleanup; newer rounds use a different key */ }
        };
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            release.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { release.run(); }
        });
    }
}
