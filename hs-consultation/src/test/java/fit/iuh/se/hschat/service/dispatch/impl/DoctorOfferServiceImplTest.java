package fit.iuh.se.hschat.service.dispatch.impl;

import fit.iuh.se.hschat.entity.*;
import fit.iuh.se.hschat.entity.enums.*;
import fit.iuh.se.hschat.repository.*;
import fit.iuh.se.hschat.service.dispatch.DoctorDispatchSelectionService;
import fit.iuh.se.hschat.service.dispatch.offer.*;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsoperations.event.OperationalEventPublisher;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.*;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import fit.iuh.se.hsbilling.service.ConsultationCreditService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorOfferServiceImplTest {
    @Mock DoctorDispatchSelectionService selection;
    @Mock DoctorOfferStore store;
    @Mock ConsultationQueueEntryRepository queues;
    @Mock ConsultationRequestRepository requests;
    @Mock DoctorCareProfileRepository profiles;
    @Mock ConsultationSessionRepository sessions;
    @Mock UserAccountRepository users;
    @Mock OperationalEventPublisher operations;
    @Mock ApplicationEventPublisher events;
    @Mock ConsultationCreditService consultationCredits;
    DoctorOfferServiceImpl service;
    Instant now = Instant.parse("2026-09-05T05:00:00Z");

    @BeforeEach void setUp() {
        service = new DoctorOfferServiceImpl(selection, store, queues, requests, profiles, sessions, users,
                operations, events, consultationCredits);
        ReflectionTestUtils.setField(service, "clock", Clock.fixed(now, ZoneOffset.UTC));
        ReflectionTestUtils.setField(service, "doctorOfferMinutes", 5L);
        ReflectionTestUtils.setField(service, "memberConfirmationMinutes", 15L);
    }

    @Test void committedDispatchCreatesHoldMovesQueueAndAdvancesPointerWithoutSession() {
        ConsultationDispatchState state = ConsultationDispatchState.builder().id(1L).build();
        ConsultationQueueEntry entry = queue(ConsultationQueueStatus.WAITING);
        DoctorCareProfile doctor = doctor(DoctorDispatchStatus.AVAILABLE);
        when(selection.lockDispatchStateForOfferCommit()).thenReturn(state);
        when(queues.findFirstByStatusForUpdate(ConsultationQueueStatus.WAITING)).thenReturn(Optional.of(entry));
        when(selection.previewNextEligibleDoctor()).thenReturn(Optional.of(doctor));
        when(selection.lockAndRevalidateEligibleDoctor(20L)).thenReturn(Optional.of(doctor));
        when(store.create(any())).thenReturn(DoctorOfferStore.CreateResult.CREATED);

        assertTrue(service.dispatchOne());
        assertEquals(ConsultationQueueStatus.OFFERING_DOCTOR, entry.getStatus());
        ArgumentCaptor<DoctorOffer> offer = ArgumentCaptor.forClass(DoctorOffer.class);
        verify(store).create(offer.capture());
        assertEquals(now.plusSeconds(300), offer.getValue().doctorOfferExpiresAt());
        verify(selection).advanceLockedPointerAfterOfferCommit(state, 20L);
        verifyNoInteractions(sessions);
    }

    @Test void dispatchStartsANewTransactionWhenTriggeredAfterCommit() throws NoSuchMethodException {
        Transactional transaction = DoctorOfferServiceImpl.class
                .getMethod("dispatchOne")
                .getAnnotation(Transactional.class);

        assertNotNull(transaction);
        assertEquals(Propagation.REQUIRES_NEW, transaction.propagation());
    }

    @Test void absentQueueOrDoctorLeavesStateUntouched() {
        when(queues.findFirstByStatusForUpdate(ConsultationQueueStatus.WAITING)).thenReturn(Optional.empty());
        assertFalse(service.dispatchOne());
        verify(store, never()).create(any());
    }

    @Test void redisFailureDoesNotMoveWaitingQueue() {
        ConsultationQueueEntry entry = queue(ConsultationQueueStatus.WAITING);
        DoctorCareProfile doctor = doctor(DoctorDispatchStatus.AVAILABLE);
        when(queues.findFirstByStatusForUpdate(any())).thenReturn(Optional.of(entry));
        when(selection.previewNextEligibleDoctor()).thenReturn(Optional.of(doctor));
        when(selection.lockAndRevalidateEligibleDoctor(20L)).thenReturn(Optional.of(doctor));
        when(store.create(any())).thenThrow(new AppException(
                fit.iuh.se.hsshared.advice.entity.enums.ErrorCode.DISPATCH_TEMPORARILY_UNAVAILABLE));
        assertThrows(AppException.class, service::dispatchOne);
        assertEquals(ConsultationQueueStatus.WAITING, entry.getStatus());
        verify(queues, never()).save(any());
    }

    @Test void acceptStartsImmutableFifteenMinuteWindowAndDuplicateIsIdempotent() {
        DoctorOffer offered = offer(DoctorOfferState.OFFERED_TO_DOCTOR, null, null);
        DoctorOffer accepted = offer(DoctorOfferState.WAITING_MEMBER_CONFIRMATION, now, now.plusSeconds(900));
        ConsultationQueueEntry entry = queue(ConsultationQueueStatus.OFFERING_DOCTOR);
        ConsultationRequest request = request(ConsultationRequestStatus.QUEUED);
        stubOwned(offered, entry, request);
        when(users.findByIdForUpdate(20L)).thenReturn(Optional.of(account()));
        when(profiles.findByDoctorIdForUpdate(20L)).thenReturn(Optional.of(doctor(DoctorDispatchStatus.AVAILABLE)));
        when(store.accept(eq("offer-a"), eq(20L), eq(now), eq(now.plusSeconds(900))))
                .thenReturn(DoctorOfferStore.AcceptResult.ACCEPTED);
        when(store.findById("offer-a")).thenReturn(Optional.of(offered), Optional.of(accepted));

        var first = service.accept(20L, "offer-a");
        assertEquals(ConsultationQueueStatus.WAITING_MEMBER_CONFIRMATION, entry.getStatus());
        assertEquals(now.plusSeconds(900), first.memberConfirmExpiresAt());

        when(store.findById("offer-a")).thenReturn(Optional.of(accepted));
        var duplicate = service.accept(20L, "offer-a");
        assertEquals(first.memberConfirmExpiresAt(), duplicate.memberConfirmExpiresAt());
        verify(store, times(1)).accept(anyString(), anyLong(), any(), any());
    }

    @Test void rejectMakesDoctorUnavailableAndRestoresSameQueueEntry() {
        DoctorOffer offer = offer(DoctorOfferState.OFFERED_TO_DOCTOR, null, null);
        ConsultationQueueEntry entry = queue(ConsultationQueueStatus.OFFERING_DOCTOR);
        ConsultationRequest request = request(ConsultationRequestStatus.QUEUED);
        stubOwned(offer, entry, request);
        DoctorCareProfile doctor = doctor(DoctorDispatchStatus.AVAILABLE);
        when(profiles.findByDoctorIdForUpdate(20L)).thenReturn(Optional.of(doctor));
        when(store.release(offer)).thenReturn(true);
        service.reject(20L, "offer-a");
        assertEquals(ConsultationQueueStatus.WAITING, entry.getStatus());
        assertEquals(7L, entry.getQueueNumber());
        assertEquals(DoctorDispatchStatus.UNAVAILABLE, doctor.getDispatchStatus());
        assertEquals(ConsultationRequestStatus.QUEUED, request.getStatus());
    }

    @Test void anotherDoctorCannotReadOrRespondToOffer() {
        DoctorOffer offer = offer(DoctorOfferState.OFFERED_TO_DOCTOR, null, null);
        when(store.findById("offer-a")).thenReturn(Optional.of(offer));
        assertThrows(AppException.class, () -> service.accept(99L, "offer-a"));
        assertThrows(AppException.class, () -> service.reject(99L, "offer-a"));
        verify(queues, never()).save(any());
    }

    @Test void doctorCurrentOfferIsEmptyOnceQueueSessionExistsAndStaleHoldIsCleaned() {
        DoctorOffer confirmed = offer(DoctorOfferState.CONFIRMED, now.minusSeconds(60), now.plusSeconds(600));
        when(store.findByDoctorId(20L)).thenReturn(Optional.of(confirmed));
        when(sessions.findByRequestId(confirmed.requestId())).thenReturn(Optional.of(
                ConsultationSession.builder().id(500L).requestId(confirmed.requestId()).doctorId(20L)
                        .flowType(ConsultationFlowType.QUEUE_DISPATCH_V1).status(ConsultationStatus.ACTIVE).build()));
        assertNull(service.getCurrentOffer(20L));
        verify(store).release(confirmed);
        verifyNoInteractions(requests);
    }

    @Test void acceptedOfferCannotBeRejected() {
        DoctorOffer accepted = offer(DoctorOfferState.WAITING_MEMBER_CONFIRMATION, now, now.plusSeconds(900));
        when(store.findById("offer-a")).thenReturn(Optional.of(accepted));
        when(store.findByDoctorId(20L)).thenReturn(Optional.of(accepted));
        assertThrows(AppException.class, () -> service.reject(20L, "offer-a"));
        verify(store, never()).release(any());
    }

    @Test void doctorTimeoutRestoresPriorityAndPenalizesDoctor() {
        DoctorOffer offer = new DoctorOffer("offer-a", 10L, 11L, 12L, 20L,
                DoctorOfferState.OFFERED_TO_DOCTOR, now.minusSeconds(301), now, null, null);
        ConsultationQueueEntry entry = queue(ConsultationQueueStatus.OFFERING_DOCTOR);
        DoctorCareProfile doctor = doctor(DoctorDispatchStatus.AVAILABLE);
        when(store.findById("offer-a")).thenReturn(Optional.of(offer));
        when(queues.findByIdForUpdate(10L)).thenReturn(Optional.of(entry));
        when(profiles.findByDoctorIdForUpdate(20L)).thenReturn(Optional.of(doctor));
        when(store.release(offer)).thenReturn(true);
        service.processDoctorTimeout("offer-a");
        assertEquals(ConsultationQueueStatus.WAITING, entry.getStatus());
        assertEquals(7L, entry.getQueueNumber());
        assertEquals(DoctorDispatchStatus.UNAVAILABLE, doctor.getDispatchStatus());
    }

    @Test void memberTimeoutTerminatesTurnAndReleasesDoctorWithoutRequeue() {
        DoctorOffer offer = offer(DoctorOfferState.WAITING_MEMBER_CONFIRMATION,
                now.minusSeconds(901), now);
        ConsultationQueueEntry entry = queue(ConsultationQueueStatus.WAITING_MEMBER_CONFIRMATION);
        ConsultationRequest request = request(ConsultationRequestStatus.QUEUED);
        DoctorCareProfile doctor = doctor(DoctorDispatchStatus.AVAILABLE);
        when(store.findById("offer-a")).thenReturn(Optional.of(offer));
        when(queues.findByIdForUpdate(10L)).thenReturn(Optional.of(entry));
        when(requests.findByIdForUpdate(11L)).thenReturn(Optional.of(request));
        when(profiles.findByDoctorIdForUpdate(20L)).thenReturn(Optional.of(doctor));
        when(store.release(offer)).thenReturn(true);
        service.processMemberConfirmationTimeout("offer-a");
        assertEquals(ConsultationQueueStatus.TIMED_OUT, entry.getStatus());
        assertEquals(ConsultationRequestStatus.TIMED_OUT, request.getStatus());
        assertNotEquals(ConsultationQueueStatus.WAITING, entry.getStatus());
        assertEquals(DoctorDispatchStatus.AVAILABLE, doctor.getDispatchStatus());
        verifyNoInteractions(sessions);
    }

    @Test void memberTimeoutReleasesPaidCreditHold() {
        DoctorOffer offer = offer(DoctorOfferState.WAITING_MEMBER_CONFIRMATION,
                now.minusSeconds(901), now);
        ConsultationQueueEntry entry = queue(ConsultationQueueStatus.WAITING_MEMBER_CONFIRMATION);
        ConsultationRequest request = request(ConsultationRequestStatus.QUEUED);
        request.setCreditPolicy(ConsultationCreditPolicy.PER_SESSION_V1);
        request.setCreditCost(1L);
        DoctorCareProfile doctor = doctor(DoctorDispatchStatus.AVAILABLE);
        when(store.findById("offer-a")).thenReturn(Optional.of(offer));
        when(requests.findByIdForUpdate(11L)).thenReturn(Optional.of(request));
        when(queues.findByIdForUpdate(10L)).thenReturn(Optional.of(entry));
        when(profiles.findByDoctorIdForUpdate(20L)).thenReturn(Optional.of(doctor));
        when(store.release(offer)).thenReturn(true);

        service.processMemberConfirmationTimeout("offer-a");

        verify(consultationCredits).release(12L, 11L, "MEMBER_CONFIRMATION_TIMEOUT");
    }

    @Test void missingRedisOfferReconcilesBothTemporaryDbStatesToWaitingWithoutSession() {
        ConsultationQueueEntry offering = queue(ConsultationQueueStatus.OFFERING_DOCTOR);
        ConsultationQueueEntry accepted = ConsultationQueueEntry.builder().id(30L).requestId(31L).memberId(32L)
                .queueDate(LocalDate.of(2026,9,5)).queueNumber(8L).queuedAt(now.minusSeconds(500))
                .status(ConsultationQueueStatus.WAITING_MEMBER_CONFIRMATION).build();
        when(queues.findByStatusIn(anyCollection())).thenReturn(List.of(offering, accepted));
        when(store.findByQueueEntryId(anyLong())).thenReturn(Optional.empty());
        service.reconcileMissingOffers();
        assertEquals(ConsultationQueueStatus.WAITING, offering.getStatus());
        assertEquals(ConsultationQueueStatus.WAITING, accepted.getStatus());
        verify(queues, times(2)).save(any());
        verifyNoInteractions(sessions);
    }

    @Test void confirmedRedisAfterDbRollbackReturnsToMemberConfirmationWithoutCreatingSession() {
        ConsultationQueueEntry entry = queue(ConsultationQueueStatus.WAITING_MEMBER_CONFIRMATION);
        DoctorOffer confirmed = offer(DoctorOfferState.CONFIRMED, now.minusSeconds(60), now.plusSeconds(600));
        when(queues.findByStatusIn(anyCollection())).thenReturn(List.of(entry));
        when(store.findByQueueEntryId(entry.getId())).thenReturn(Optional.of(confirmed));
        when(sessions.findByRequestId(entry.getRequestId())).thenReturn(Optional.empty());
        service.reconcileMissingOffers();
        verify(store).restoreMemberConfirmation(confirmed);
        assertEquals(ConsultationQueueStatus.WAITING_MEMBER_CONFIRMATION, entry.getStatus());
        verify(queues, never()).save(entry);
    }

    @Test void fulfilledSessionIsAuthoritativeAndDeadlineCleanupOnlyRemovesStaleRedisOffer() {
        DoctorOffer confirmed = offer(DoctorOfferState.CONFIRMED, now.minusSeconds(60), now);
        when(store.findById(confirmed.offerId())).thenReturn(Optional.of(confirmed));
        when(sessions.findByRequestId(confirmed.requestId())).thenReturn(Optional.of(
                ConsultationSession.builder().id(500L).requestId(confirmed.requestId())
                        .flowType(ConsultationFlowType.QUEUE_DISPATCH_V1).status(ConsultationStatus.ACTIVE).build()));
        service.processMemberConfirmationTimeout(confirmed.offerId());
        verify(store).release(confirmed);
        verifyNoInteractions(queues, requests);
        verifyNoInteractions(profiles);
    }

    private void stubOwned(DoctorOffer offer, ConsultationQueueEntry entry, ConsultationRequest request) {
        when(store.findById("offer-a")).thenReturn(Optional.of(offer));
        when(store.findByDoctorId(20L)).thenReturn(Optional.of(offer));
        when(queues.findByIdForUpdate(10L)).thenReturn(Optional.of(entry));
        when(requests.findByIdForUpdate(11L)).thenReturn(Optional.of(request));
    }
    private DoctorOffer offer(DoctorOfferState state, Instant acceptedAt, Instant memberDeadline) {
        return new DoctorOffer("offer-a", 10L, 11L, 12L, 20L, state,
                now.minusSeconds(60), now.plusSeconds(240), acceptedAt, memberDeadline);
    }
    private ConsultationQueueEntry queue(ConsultationQueueStatus status) {
        return ConsultationQueueEntry.builder().id(10L).requestId(11L).memberId(12L)
                .queueDate(LocalDate.of(2026,9,5)).queueNumber(7L).queuedAt(now.minusSeconds(600))
                .status(status).build();
    }
    private ConsultationRequest request(ConsultationRequestStatus status) {
        return ConsultationRequest.builder().id(11L).memberId(12L).flowType(ConsultationFlowType.QUEUE_DISPATCH_V1)
                .reason("help").status(status).build();
    }
    private DoctorCareProfile doctor(DoctorDispatchStatus status) {
        return DoctorCareProfile.builder().id(21L).doctorId(20L).dispatchStatus(status)
                .stopAfterCurrentSession(false).build();
    }
    private UserAccount account() {
        return UserAccount.builder().id(20L).email("doctor@example.com").passwordHash("hash")
                .role(UserRole.DOCTOR).status(AccountStatus.ACTIVE).build();
    }
}
