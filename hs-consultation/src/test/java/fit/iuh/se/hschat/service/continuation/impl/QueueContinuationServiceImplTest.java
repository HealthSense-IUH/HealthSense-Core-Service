package fit.iuh.se.hschat.service.continuation.impl;

import fit.iuh.se.hschat.dto.request.SubmitContinuationDecisionRequest;
import fit.iuh.se.hschat.dto.response.ContinuationDecisionResponse;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.*;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.service.continuation.*;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsoperations.event.OperationalEventPublisher;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueueContinuationServiceImplTest {

    static final Instant ENDS_AT = Instant.parse("2026-09-06T03:15:00Z");
    static final Long SESSION_ID = 50L;
    static final Long MEMBER_ID = 10L;
    static final Long DOCTOR_ID = 20L;

    @Mock ConsultationSessionRepository sessionRepository;
    @Mock ContinuationStore store;
    @Mock QueueSessionCompletionService completionService;
    @Mock OperationalEventPublisher events;

    QueueContinuationServiceImpl service;
    Instant currentNow;

    @BeforeEach
    void setUp() {
        service = new QueueContinuationServiceImpl(sessionRepository, store, completionService, events);
        setNow(ENDS_AT);
    }

    @Test
    void beforeBlockEndHasNoContinuationAndDoesNotTouchRedis() {
        setNow(ENDS_AT.minusMillis(1));
        when(sessionRepository.findByIdForUpdate(SESSION_ID)).thenReturn(Optional.of(session()));

        assertNull(service.getCurrent(MEMBER_ID, UserRole.MEMBER, SESSION_ID));
        verifyNoInteractions(store);
    }

    @Test
    void atBlockEndOpensRoundWithDeadlinesDerivedFromOriginalEndAndNotifiesOnce() {
        ConsultationSession session = session();
        ContinuationState state = pendingState();
        when(sessionRepository.findByIdForUpdate(SESSION_ID)).thenReturn(Optional.of(session));
        when(store.open(SESSION_ID, 0, ENDS_AT, ENDS_AT.plusSeconds(300)))
                .thenReturn(ContinuationStore.OpenResult.CREATED);
        when(store.find(SESSION_ID, 0)).thenReturn(Optional.of(state));

        ContinuationDecisionResponse response = service.getCurrent(MEMBER_ID, UserRole.MEMBER, SESSION_ID);

        assertEquals(ENDS_AT, response.promptedAt());
        assertEquals(ENDS_AT.plusSeconds(300), response.graceExpiresAt());
        verify(events).record(argThat(command ->
                command.eventType() == fit.iuh.se.hsoperations.entity.enums.BusinessEventType.SESSION_CONTINUATION_REQUESTED
                        && command.notifications().size() == 2));
    }

    @Test
    void duplicateOpenDoesNotDuplicatePromptNotification() {
        when(sessionRepository.findByIdForUpdate(SESSION_ID)).thenReturn(Optional.of(session()));
        when(store.open(anyLong(), anyInt(), any(), any()))
                .thenReturn(ContinuationStore.OpenResult.ALREADY_OPEN);
        when(store.find(SESSION_ID, 0)).thenReturn(Optional.of(pendingState()));

        service.getCurrent(DOCTOR_ID, UserRole.DOCTOR, SESSION_ID);

        verifyNoInteractions(events);
    }

    @Test
    void earlyDecisionIsRejected() {
        setNow(ENDS_AT.minusMillis(1));
        when(sessionRepository.findByIdForUpdate(SESSION_ID)).thenReturn(Optional.of(session()));

        assertThrows(AppException.class, () -> service.decide(MEMBER_ID, UserRole.MEMBER, SESSION_ID, 0,
                decision(ContinuationDecision.CONTINUE)));
        verifyNoInteractions(store);
    }

    @Test
    void exactGraceDeadlineRejectsDecisionAndNeverExtends() {
        setNow(ENDS_AT.plusSeconds(300));
        when(sessionRepository.findByIdForUpdate(SESSION_ID)).thenReturn(Optional.of(session()));

        assertThrows(AppException.class, () -> service.decide(DOCTOR_ID, UserRole.DOCTOR, SESSION_ID, 0,
                decision(ContinuationDecision.CONTINUE)));
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void unrelatedAndWrongRoleActorsAreDenied() {
        when(sessionRepository.findByIdForUpdate(SESSION_ID)).thenReturn(Optional.of(session()));

        assertThrows(AppException.class,
                () -> service.getCurrent(999L, UserRole.MEMBER, SESSION_ID));
        assertThrows(AppException.class,
                () -> service.getCurrent(MEMBER_ID, UserRole.DOCTOR, SESSION_ID));
        assertThrows(AppException.class,
                () -> service.getCurrent(MEMBER_ID, UserRole.CARE_COORDINATOR, SESSION_ID));
    }

    @Test
    void oneContinueDoesNotExtendAndDuplicateDecisionIsIdempotent() {
        ContinuationSessionFixture fixture = fixture(ContinuationStore.DecisionResult.IDEMPOTENT,
                new ContinuationState(SESSION_ID, 0, ContinuationDecision.PENDING,
                        ContinuationDecision.CONTINUE, ENDS_AT, ENDS_AT.plusSeconds(300)));

        ContinuationDecisionResponse response = service.decide(MEMBER_ID, UserRole.MEMBER, SESSION_ID, 0,
                decision(ContinuationDecision.CONTINUE));

        assertEquals(0, response.continuationRound());
        assertEquals(ENDS_AT, response.endsAt());
        verify(sessionRepository, never()).save(any());
        verifyNoInteractions(events);
        assertNotNull(fixture);
    }

    @Test
    void conflictingSecondDecisionIsRejectedAndOriginalStateRemains() {
        fixture(ContinuationStore.DecisionResult.CONFLICT,
                new ContinuationState(SESSION_ID, 0, ContinuationDecision.PENDING,
                        ContinuationDecision.CONTINUE, ENDS_AT, ENDS_AT.plusSeconds(300)));

        assertThrows(AppException.class, () -> service.decide(MEMBER_ID, UserRole.MEMBER, SESSION_ID, 0,
                decision(ContinuationDecision.STOP)));
        verifyNoInteractions(completionService);
    }

    @Test
    void secondContinueStartsFullNewBlockAndIncrementsRoundExactlyOnce() {
        Instant secondDecisionAt = ENDS_AT.plusSeconds(120);
        setNow(secondDecisionAt);
        ContinuationState both = new ContinuationState(SESSION_ID, 0, ContinuationDecision.CONTINUE,
                ContinuationDecision.CONTINUE, ENDS_AT, ENDS_AT.plusSeconds(300));
        ContinuationSessionFixture fixture = fixture(ContinuationStore.DecisionResult.ACCEPTED, both);

        ContinuationDecisionResponse response = service.decide(DOCTOR_ID, UserRole.DOCTOR, SESSION_ID, 0,
                decision(ContinuationDecision.CONTINUE));

        assertEquals(ConsultationStatus.ACTIVE, response.sessionStatus());
        assertEquals(1, response.continuationRound());
        assertEquals(secondDecisionAt, response.blockStartedAt());
        assertEquals(secondDecisionAt.plusSeconds(900), response.endsAt());
        assertEquals(response.endsAt(), fixture.session().getSupportEndsAt());
        verify(sessionRepository).save(fixture.session());
        verify(store).release(SESSION_ID, 0);
        verify(events, times(2)).record(any());
    }

    @Test
    void participantStopCompletesImmediatelyThroughCentralService() {
        ContinuationState stopped = new ContinuationState(SESSION_ID, 0, ContinuationDecision.PENDING,
                ContinuationDecision.STOP, ENDS_AT, ENDS_AT.plusSeconds(300));
        fixture(ContinuationStore.DecisionResult.ACCEPTED, stopped);
        ContinuationDecisionResponse completed = mock(ContinuationDecisionResponse.class);
        when(completionService.complete(SESSION_ID, 0, ENDS_AT,
                ConsultationCompletionReason.CONTINUATION_STOPPED)).thenReturn(completed);

        assertSame(completed, service.decide(MEMBER_ID, UserRole.MEMBER, SESSION_ID, 0,
                decision(ContinuationDecision.STOP)));
    }

    @Test
    void staleRoundAndLegacySessionAreRejected() {
        ConsultationSession session = session();
        session.setContinuationRound(1);
        when(sessionRepository.findByIdForUpdate(SESSION_ID)).thenReturn(Optional.of(session));
        assertThrows(AppException.class, () -> service.decide(MEMBER_ID, UserRole.MEMBER, SESSION_ID, 0,
                decision(ContinuationDecision.CONTINUE)));

        session.setFlowType(ConsultationFlowType.LEGACY_V3);
        assertThrows(AppException.class,
                () -> service.getCurrent(MEMBER_ID, UserRole.MEMBER, SESSION_ID));
    }

    @Test
    void stopAfterGraceTimeoutCompletionIsRejectedRatherThanTreatedAsDuplicate() {
        ConsultationSession session = session();
        session.setStatus(ConsultationStatus.COMPLETED);
        session.setCompletionReason(ConsultationCompletionReason.CONTINUATION_GRACE_EXPIRED);
        when(sessionRepository.findByIdForUpdate(SESSION_ID)).thenReturn(Optional.of(session));

        assertThrows(AppException.class, () -> service.decide(MEMBER_ID, UserRole.MEMBER, SESSION_ID, 0,
                decision(ContinuationDecision.STOP)));
        verifyNoInteractions(store, completionService);
    }

    @Test
    void schedulerRecreatesMissingStateWithoutRestartingGrace() {
        Instant lateTick = ENDS_AT.plusSeconds(3);
        when(sessionRepository.findByIdForUpdate(SESSION_ID)).thenReturn(Optional.of(session()));
        when(store.open(SESSION_ID, 0, ENDS_AT, ENDS_AT.plusSeconds(300)))
                .thenReturn(ContinuationStore.OpenResult.CREATED);
        when(store.find(SESSION_ID, 0)).thenReturn(Optional.of(pendingState()));

        service.processDeadline(SESSION_ID, lateTick);

        verify(store).open(SESSION_ID, 0, ENDS_AT, ENDS_AT.plusSeconds(300));
    }

    @Test
    void schedulerAtGraceDeadlineCompletesAndDoesNotOpenFreshGrace() {
        Instant deadline = ENDS_AT.plusSeconds(300);
        when(sessionRepository.findByIdForUpdate(SESSION_ID)).thenReturn(Optional.of(session()));

        service.processDeadline(SESSION_ID, deadline);

        verify(completionService).complete(SESSION_ID, 0, deadline,
                ConsultationCompletionReason.CONTINUATION_GRACE_EXPIRED);
        verifyNoInteractions(store);
    }

    private ContinuationSessionFixture fixture(
            ContinuationStore.DecisionResult result, ContinuationState outcomeState) {
        ConsultationSession session = session();
        when(sessionRepository.findByIdForUpdate(SESSION_ID)).thenReturn(Optional.of(session));
        when(store.open(anyLong(), anyInt(), any(), any()))
                .thenReturn(ContinuationStore.OpenResult.ALREADY_OPEN);
        when(store.find(SESSION_ID, 0)).thenReturn(Optional.of(pendingState()));
        UserRole role = outcomeState.doctorDecision() == ContinuationDecision.CONTINUE
                ? UserRole.DOCTOR : UserRole.MEMBER;
        when(store.decide(eq(SESSION_ID), eq(0), eq(role), any(ContinuationDecision.class),
                eq(currentNow)))
                .thenReturn(new ContinuationStore.DecisionOutcome(result, outcomeState));
        return new ContinuationSessionFixture(session);
    }

    private ConsultationSession session() {
        return ConsultationSession.builder().id(SESSION_ID).memberId(MEMBER_ID).doctorId(DOCTOR_ID)
                .flowType(ConsultationFlowType.QUEUE_DISPATCH_V1).status(ConsultationStatus.ACTIVE)
                .startedAt(ENDS_AT.minusSeconds(900)).blockStartedAt(ENDS_AT.minusSeconds(900))
                .endsAt(ENDS_AT).supportEndsAt(ENDS_AT).continuationRound(0).build();
    }

    private ContinuationState pendingState() {
        return new ContinuationState(SESSION_ID, 0, ContinuationDecision.PENDING,
                ContinuationDecision.PENDING, ENDS_AT, ENDS_AT.plusSeconds(300));
    }

    private SubmitContinuationDecisionRequest decision(ContinuationDecision decision) {
        return new SubmitContinuationDecisionRequest(decision);
    }

    private void setNow(Instant now) {
        currentNow = now;
        ReflectionTestUtils.setField(service, "clock", Clock.fixed(now, ZoneOffset.UTC));
    }

    private record ContinuationSessionFixture(ConsultationSession session) {}
}
