package fit.iuh.se.hschat.service.session.impl;

import fit.iuh.se.hschat.dto.response.ConsultationSessionResponse;
import fit.iuh.se.hschat.entity.*;
import fit.iuh.se.hschat.entity.enums.*;
import fit.iuh.se.hschat.mapper.ConsultationMapper;
import fit.iuh.se.hschat.repository.*;
import fit.iuh.se.hschat.service.authorization.EpisodeHealthRecordAuthorizationService;
import fit.iuh.se.hschat.service.dispatch.DoctorDispatchSelectionService;
import fit.iuh.se.hschat.service.dispatch.offer.*;
import fit.iuh.se.hsoperations.dto.command.OperationalEventCommand;
import fit.iuh.se.hsoperations.event.OperationalEventPublisher;
import fit.iuh.se.hsbilling.service.ConsultationCreditService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.*;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueueConsultationSessionServiceImplTest {
    @Mock DoctorDispatchSelectionService dispatch;
    @Mock UserAccountRepository users;
    @Mock ConsultationRequestRepository requests;
    @Mock ConsultationQueueEntryRepository queues;
    @Mock DoctorCareProfileRepository doctors;
    @Mock ConsultationSessionRepository sessions;
    @Mock ConsultationParticipantRepository participants;
    @Mock EpisodeHealthRecordAuthorizationService authorizations;
    @Mock DoctorOfferStore offers;
    @Mock ConsultationMapper mapper;
    @Mock OperationalEventPublisher events;
    @Mock ApplicationEventPublisher applicationEvents;
    @Mock ConsultationCreditService consultationCredits;
    QueueConsultationSessionServiceImpl service;
    final Instant now = Instant.parse("2026-09-05T06:00:00Z");

    @BeforeEach void setUp() {
        service = new QueueConsultationSessionServiceImpl(dispatch, users, requests, queues, doctors,
                sessions, participants, authorizations, offers, mapper, events, applicationEvents,
                consultationCredits);
        ReflectionTestUtils.setField(service, "clock", Clock.fixed(now, ZoneOffset.UTC));
        ReflectionTestUtils.setField(service, "initialSessionMinutes", 15L);
    }

    @Test void validConfirmationCreatesOneActiveQueueSessionAndDurableBoundary() {
        Fixtures f = validFixtures();
        when(sessions.saveAndFlush(any())).thenAnswer(invocation -> {
            ConsultationSession session = invocation.getArgument(0); session.setId(500L); return session;
        });
        when(mapper.toSessionResponse(any())).thenReturn(ConsultationSessionResponse.builder().id(500L).build());

        ConsultationSessionResponse response = service.confirmMember(12L, 11L, "offer-a");

        assertEquals(500L, response.getId());
        ArgumentCaptor<ConsultationSession> sessionCaptor = ArgumentCaptor.forClass(ConsultationSession.class);
        verify(sessions).saveAndFlush(sessionCaptor.capture());
        ConsultationSession session = sessionCaptor.getValue();
        assertEquals(ConsultationFlowType.QUEUE_DISPATCH_V1, session.getFlowType());
        assertEquals(ConsultationStatus.ACTIVE, session.getStatus());
        assertEquals(now, session.getStartedAt());
        assertEquals(now, session.getActivatedAt());
        assertEquals(now, session.getBlockStartedAt());
        assertEquals(now.plusSeconds(900), session.getEndsAt());
        assertEquals(0, session.getContinuationRound());
        assertEquals(ConsultationRequestStatus.FULFILLED, f.request.getStatus());
        assertEquals(500L, f.request.getConsultationSessionId());
        assertEquals(ConsultationQueueStatus.FULFILLED, f.queue.getStatus());
        assertEquals(now, f.queue.getFulfilledAt());
        assertEquals(DoctorDispatchStatus.BUSY, f.doctor.getDispatchStatus());
        assertEquals(500L, f.doctor.getBusySessionId());
        assertTrue(f.doctor.getStopAfterCurrentSession());
        verify(participants, times(2)).save(any());
        verify(authorizations).authorizeInitialRecords(session, List.of(101L, 102L));
        verify(events, times(3)).record(any(OperationalEventCommand.class));
    }

    @Test void paidConfirmationCapturesCreditAndCopiesPolicyToSession() {
        Fixtures fixtures = validFixtures();
        fixtures.request.setCreditPolicy(ConsultationCreditPolicy.PER_SESSION_V1);
        fixtures.request.setCreditCost(1L);
        when(sessions.saveAndFlush(any())).thenAnswer(invocation -> {
            ConsultationSession session = invocation.getArgument(0); session.setId(500L); return session;
        });
        when(mapper.toSessionResponse(any())).thenReturn(ConsultationSessionResponse.builder().id(500L).build());

        ConsultationSessionResponse response = service.confirmMember(12L, 11L, "offer-a");

        verify(consultationCredits).capture(12L, 11L, 500L);
        verify(sessions).saveAndFlush(argThat(session ->
                session.getCreditPolicy() == ConsultationCreditPolicy.PER_SESSION_V1
                        && session.getCreditCost() == 1L));
        assertEquals(fit.iuh.se.hsbilling.entity.enums.CreditReservationStatus.CAPTURED,
                response.getCreditReservationStatus());
    }

    @Test void confirmationChargesV2CreditOnlyWhenSessionIsCreated() {
        Fixtures fixtures = validFixtures();
        fixtures.request.setCreditPolicy(ConsultationCreditPolicy.PER_SESSION_CONFIRM_V2);
        fixtures.request.setCreditCost(1L);
        when(sessions.saveAndFlush(any())).thenAnswer(invocation -> {
            ConsultationSession session = invocation.getArgument(0); session.setId(500L); return session;
        });
        when(mapper.toSessionResponse(any())).thenReturn(ConsultationSessionResponse.builder().id(500L).build());

        ConsultationSessionResponse response = service.confirmMember(12L, 11L, "offer-a");

        verify(consultationCredits).chargeSession(12L, 11L, 500L, 1L);
        verify(consultationCredits, never()).capture(anyLong(), anyLong(), anyLong());
        verify(sessions).saveAndFlush(argThat(session ->
                session.getCreditPolicy() == ConsultationCreditPolicy.PER_SESSION_CONFIRM_V2
                        && session.getCreditCost() == 1L));
        assertEquals(fit.iuh.se.hsbilling.entity.enums.CreditReservationStatus.CAPTURED,
                response.getCreditReservationStatus());
    }

    @Test void duplicateAfterCommitReturnsExistingSessionWithoutRedisOrDuplicateSideEffects() {
        ConsultationRequest request = request(ConsultationRequestStatus.FULFILLED);
        request.setConsultationSessionId(500L);
        ConsultationSession existing = ConsultationSession.builder().id(500L).requestId(11L).memberId(12L)
                .doctorId(20L).flowType(ConsultationFlowType.QUEUE_DISPATCH_V1)
                .status(ConsultationStatus.ACTIVE).endsAt(now.plusSeconds(800)).build();
        when(users.findByIdForUpdate(12L)).thenReturn(Optional.of(account(12L, UserRole.MEMBER)));
        when(requests.findByIdForUpdate(11L)).thenReturn(Optional.of(request));
        when(sessions.findByRequestId(11L)).thenReturn(Optional.of(existing));
        when(queues.findByRequestId(11L)).thenReturn(Optional.empty());
        when(mapper.toSessionResponse(existing)).thenReturn(ConsultationSessionResponse.builder().id(500L).build());

        assertEquals(500L, service.confirmMember(12L, 11L, "already-cleaned").getId());
        verify(offers, never()).findById(anyString());
        verify(sessions, never()).saveAndFlush(any());
        verifyNoInteractions(participants, authorizations, events);
    }

    @Test void duplicatePaidConfirmationVerifiesTheSameCapturedReservation() {
        ConsultationRequest request = request(ConsultationRequestStatus.FULFILLED);
        request.setCreditPolicy(ConsultationCreditPolicy.PER_SESSION_V1);
        request.setCreditCost(1L);
        request.setConsultationSessionId(500L);
        ConsultationSession existing = ConsultationSession.builder().id(500L).requestId(11L).memberId(12L)
                .doctorId(20L).flowType(ConsultationFlowType.QUEUE_DISPATCH_V1)
                .creditPolicy(ConsultationCreditPolicy.PER_SESSION_V1).creditCost(1L)
                .status(ConsultationStatus.ACTIVE).endsAt(now.plusSeconds(800)).build();
        when(users.findByIdForUpdate(12L)).thenReturn(Optional.of(account(12L, UserRole.MEMBER)));
        when(requests.findByIdForUpdate(11L)).thenReturn(Optional.of(request));
        when(sessions.findByRequestId(11L)).thenReturn(Optional.of(existing));
        when(queues.findByRequestId(11L)).thenReturn(Optional.empty());
        when(mapper.toSessionResponse(existing)).thenReturn(ConsultationSessionResponse.builder().id(500L).build());

        service.confirmMember(12L, 11L, "already-cleaned");

        verify(consultationCredits).capture(12L, 11L, 500L);
        verify(sessions, never()).saveAndFlush(any());
    }

    @Test void duplicateV2ConfirmationVerifiesTheSameSessionCharge() {
        ConsultationRequest request = request(ConsultationRequestStatus.FULFILLED);
        request.setCreditPolicy(ConsultationCreditPolicy.PER_SESSION_CONFIRM_V2);
        request.setCreditCost(1L);
        request.setConsultationSessionId(500L);
        ConsultationSession existing = ConsultationSession.builder().id(500L).requestId(11L).memberId(12L)
                .doctorId(20L).flowType(ConsultationFlowType.QUEUE_DISPATCH_V1)
                .creditPolicy(ConsultationCreditPolicy.PER_SESSION_CONFIRM_V2).creditCost(1L)
                .status(ConsultationStatus.ACTIVE).endsAt(now.plusSeconds(800)).build();
        when(users.findByIdForUpdate(12L)).thenReturn(Optional.of(account(12L, UserRole.MEMBER)));
        when(requests.findByIdForUpdate(11L)).thenReturn(Optional.of(request));
        when(sessions.findByRequestId(11L)).thenReturn(Optional.of(existing));
        when(queues.findByRequestId(11L)).thenReturn(Optional.empty());
        when(mapper.toSessionResponse(existing)).thenReturn(ConsultationSessionResponse.builder().id(500L).build());

        service.confirmMember(12L, 11L, "already-cleaned");

        verify(consultationCredits).chargeSession(12L, 11L, 500L, 1L);
        verify(sessions, never()).saveAndFlush(any());
    }

    @Test void duplicateAfterCommitReturnsExistingSessionWhenRedisCleanupIsUnavailable() {
        ConsultationRequest request = request(ConsultationRequestStatus.FULFILLED);
        request.setConsultationSessionId(500L);
        ConsultationSession existing = ConsultationSession.builder().id(500L).requestId(11L).memberId(12L)
                .doctorId(20L).flowType(ConsultationFlowType.QUEUE_DISPATCH_V1)
                .status(ConsultationStatus.ACTIVE).endsAt(now.plusSeconds(800)).build();
        when(users.findByIdForUpdate(12L)).thenReturn(Optional.of(account(12L, UserRole.MEMBER)));
        when(requests.findByIdForUpdate(11L)).thenReturn(Optional.of(request));
        when(sessions.findByRequestId(11L)).thenReturn(Optional.of(existing));
        ConsultationQueueEntry fulfilled = queue(ConsultationQueueStatus.FULFILLED);
        when(queues.findByRequestId(11L)).thenReturn(Optional.of(fulfilled));
        when(offers.findByQueueEntryId(fulfilled.getId())).thenThrow(new IllegalStateException("redis unavailable"));
        when(mapper.toSessionResponse(existing)).thenReturn(ConsultationSessionResponse.builder().id(500L).build());

        assertEquals(500L, service.confirmMember(12L, 11L, "already-committed").getId());
        verify(sessions, never()).saveAndFlush(any());
        verifyNoInteractions(participants, authorizations, events);
    }

    @Test void exactMemberDeadlineRejectsWithoutCreatingDurableState() {
        Fixtures f = validFixtures();
        when(offers.confirm("offer-a", 10L, 12L, 20L, now))
                .thenReturn(DoctorOfferStore.ConfirmResult.EXPIRED);
        assertThrows(AppException.class, () -> service.confirmMember(12L, 11L, "offer-a"));
        verify(sessions, never()).saveAndFlush(any());
        assertEquals(ConsultationRequestStatus.QUEUED, f.request.getStatus());
        assertEquals(ConsultationQueueStatus.WAITING_MEMBER_CONFIRMATION, f.queue.getStatus());
        assertEquals(DoctorDispatchStatus.AVAILABLE, f.doctor.getDispatchStatus());
    }

    @Test void anotherMemberCannotConfirm() {
        when(users.findByIdForUpdate(99L)).thenReturn(Optional.of(account(99L, UserRole.MEMBER)));
        when(requests.findByIdForUpdate(11L)).thenReturn(Optional.of(request(ConsultationRequestStatus.QUEUED)));
        assertThrows(AppException.class, () -> service.confirmMember(99L, 11L, "offer-a"));
        verifyNoInteractions(offers, participants, authorizations);
    }

    @Test void cancelledRequestCannotCreateSession() {
        when(users.findByIdForUpdate(12L)).thenReturn(Optional.of(account(12L, UserRole.MEMBER)));
        when(requests.findByIdForUpdate(11L)).thenReturn(Optional.of(request(ConsultationRequestStatus.CANCELLED)));
        when(sessions.findByRequestId(11L)).thenReturn(Optional.empty());
        when(queues.findByRequestIdForUpdate(11L)).thenReturn(Optional.of(queue(ConsultationQueueStatus.CANCELLED)));
        assertThrows(AppException.class, () -> service.confirmMember(12L, 11L, "offer-a"));
        verify(sessions, never()).saveAndFlush(any());
    }

    @Test void dbRollbackRestoresAcceptedRedisOfferAndCommitReleasesIt() {
        validFixtures();
        when(sessions.saveAndFlush(any())).thenAnswer(invocation -> {
            ConsultationSession session = invocation.getArgument(0); session.setId(500L); return session;
        });
        when(mapper.toSessionResponse(any())).thenReturn(ConsultationSessionResponse.builder().id(500L).build());
        TransactionSynchronizationManager.initSynchronization();
        try {
            service.confirmMember(12L, 11L, "offer-a");
            List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
            assertEquals(1, synchronizations.size());
            synchronizations.getFirst().afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
            verify(offers).restoreMemberConfirmation(any());
            synchronizations.getFirst().afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
            verify(offers).release(any());
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private Fixtures validFixtures() {
        ConsultationRequest request = request(ConsultationRequestStatus.QUEUED);
        ConsultationQueueEntry queue = queue(ConsultationQueueStatus.WAITING_MEMBER_CONFIRMATION);
        DoctorCareProfile doctor = DoctorCareProfile.builder().doctorId(20L)
                .dispatchStatus(DoctorDispatchStatus.AVAILABLE).stopAfterCurrentSession(true).build();
        DoctorOffer offer = new DoctorOffer("offer-a", 10L, 11L, 12L, 20L,
                DoctorOfferState.WAITING_MEMBER_CONFIRMATION, now.minusSeconds(100), now.minusSeconds(1),
                now.minusSeconds(60), now.plusSeconds(840));
        when(users.findByIdForUpdate(12L)).thenReturn(Optional.of(account(12L, UserRole.MEMBER)));
        when(requests.findByIdForUpdate(11L)).thenReturn(Optional.of(request));
        when(sessions.findByRequestId(11L)).thenReturn(Optional.empty());
        when(queues.findByRequestIdForUpdate(11L)).thenReturn(Optional.of(queue));
        when(offers.findById("offer-a")).thenReturn(Optional.of(offer));
        when(offers.findByQueueEntryId(10L)).thenReturn(Optional.of(offer));
        when(offers.findByDoctorId(20L)).thenReturn(Optional.of(offer));
        when(users.findByIdForUpdate(20L)).thenReturn(Optional.of(account(20L, UserRole.DOCTOR)));
        when(doctors.findByDoctorIdForUpdate(20L)).thenReturn(Optional.of(doctor));
        when(offers.confirm("offer-a", 10L, 12L, 20L, now))
                .thenReturn(DoctorOfferStore.ConfirmResult.CONFIRMED);
        return new Fixtures(request, queue, doctor);
    }

    private ConsultationRequest request(ConsultationRequestStatus status) {
        return ConsultationRequest.builder().id(11L).memberId(12L).flowType(ConsultationFlowType.QUEUE_DISPATCH_V1)
                .status(status).selectedHealthRecordIds(new ArrayList<>(List.of(101L, 102L))).build();
    }
    private ConsultationQueueEntry queue(ConsultationQueueStatus status) {
        return ConsultationQueueEntry.builder().id(10L).requestId(11L).memberId(12L)
                .queueDate(LocalDate.of(2026, 9, 5)).queueNumber(7L).queuedAt(now.minusSeconds(1000))
                .status(status).build();
    }
    private UserAccount account(Long id, UserRole role) {
        return UserAccount.builder().id(id).role(role).status(AccountStatus.ACTIVE).build();
    }
    private record Fixtures(ConsultationRequest request, ConsultationQueueEntry queue, DoctorCareProfile doctor) {}
}
