package fit.iuh.se.hschat.service.finalsummary.impl;

import fit.iuh.se.hschat.entity.ConsultationFinalSummary;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.DoctorCareProfile;
import fit.iuh.se.hschat.entity.enums.*;
import fit.iuh.se.hschat.repository.ConsultationFinalSummaryRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.repository.DoctorCareProfileRepository;
import fit.iuh.se.hschat.service.dispatch.event.DoctorDispatchStatusChanged;
import fit.iuh.se.hschat.service.doctor.SupportScheduleValidator;
import fit.iuh.se.hsoperations.entity.enums.BusinessEventType;
import fit.iuh.se.hsoperations.entity.enums.NotificationType;
import fit.iuh.se.hsoperations.event.OperationalEventPublisher;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueueFinalSummaryLifecycleServiceImplTest {

    static final Instant COMPLETED_AT = Instant.parse("2026-09-06T03:00:00Z");
    static final Instant DUE_AT = Instant.parse("2026-09-06T03:10:00Z");

    @Mock ConsultationSessionRepository sessions;
    @Mock DoctorCareProfileRepository profiles;
    @Mock ConsultationFinalSummaryRepository summaries;
    @Mock UserAccountRepository accounts;
    @Mock SupportScheduleValidator schedules;
    @Mock OperationalEventPublisher events;
    @Mock ApplicationEventPublisher applicationEvents;

    QueueFinalSummaryLifecycleServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new QueueFinalSummaryLifecycleServiceImpl(
                sessions, profiles, summaries, accounts, schedules, events, applicationEvents);
        lenient().when(schedules.isValid(any(), any(), eq(true))).thenReturn(true);
    }

    @Test
    void timelyFinalizeReleasesOwnedEligibleDoctorToAvailableAndTriggersRedispatch() {
        ConsultationSession session = session();
        DoctorCareProfile profile = profile(false);
        ConsultationFinalSummary summary = finalized(DUE_AT.minusSeconds(1));

        service.onSummaryFinalized(session, profile, activeDoctor(), summary, DUE_AT.minusSeconds(1));

        assertEquals(FinalSummaryClosureStatus.SUMMARY_FINALIZED, session.getSummaryClosureStatus());
        assertEquals(DoctorDispatchStatus.AVAILABLE, profile.getDispatchStatus());
        assertNull(profile.getBusySessionId());
        assertEquals(DoctorReleaseReason.SUMMARY_FINALIZED, session.getDoctorReleaseReason());
        assertEquals(DUE_AT.minusSeconds(1), session.getDoctorReleasedAt());
        verify(applicationEvents).publishEvent(isA(DoctorDispatchStatusChanged.class));
        verify(events).record(argThat(command -> command.eventType() == BusinessEventType.DOCTOR_AVAILABLE));
    }

    @Test
    void stopAfterCurrentSessionPersistsAndTimelyFinalizeReleasesToUnavailable() {
        ConsultationSession session = session();
        DoctorCareProfile profile = profile(true);

        service.onSummaryFinalized(
                session, profile, activeDoctor(), finalized(DUE_AT.minusSeconds(1)), DUE_AT.minusSeconds(1));

        assertEquals(DoctorDispatchStatus.UNAVAILABLE, profile.getDispatchStatus());
        assertNull(profile.getBusySessionId());
        assertTrue(profile.getStopAfterCurrentSession());
        verify(applicationEvents).publishEvent(isA(DoctorDispatchStatusChanged.class));
    }

    @Test
    void exactDeadlineUsesTimeoutSemanticsEvenThoughSummaryFinalizes() {
        ConsultationSession session = session();
        DoctorCareProfile profile = profile(false);

        service.onSummaryFinalized(session, profile, activeDoctor(), finalized(DUE_AT), DUE_AT);

        assertEquals(FinalSummaryClosureStatus.SUMMARY_FINALIZED, session.getSummaryClosureStatus());
        assertEquals(DoctorDispatchStatus.UNAVAILABLE, profile.getDispatchStatus());
        assertEquals(DoctorReleaseReason.SUMMARY_TIMEOUT, session.getDoctorReleaseReason());
        verify(events).record(argThat(command -> command.eventType() == BusinessEventType.DOCTOR_SUMMARY_RELEASE_TIMEOUT
                && command.notifications().stream().anyMatch(notification ->
                notification.type() == NotificationType.DOCTOR_SUMMARY_DEADLINE_EXPIRED)));
    }

    @Test
    void pendingSummaryTimesOutOnceAndRemainsPending() {
        ConsultationSession session = session();
        DoctorCareProfile profile = profile(false);
        when(sessions.findByIdForUpdate(50L)).thenReturn(Optional.of(session));
        when(accounts.findByIdForUpdate(20L)).thenReturn(Optional.of(activeDoctor()));
        when(profiles.findByDoctorIdForUpdate(20L)).thenReturn(Optional.of(profile));
        when(summaries.findBySessionIdForUpdate(50L)).thenReturn(Optional.of(draft()));

        service.processDeadline(50L, DUE_AT);

        assertEquals(FinalSummaryClosureStatus.SUMMARY_PENDING, session.getSummaryClosureStatus());
        assertEquals(DoctorDispatchStatus.UNAVAILABLE, profile.getDispatchStatus());
        assertNull(profile.getBusySessionId());
        assertEquals(DoctorReleaseReason.SUMMARY_TIMEOUT, session.getDoctorReleaseReason());

        service.processDeadline(50L, DUE_AT.plusSeconds(1));
        verify(profiles, times(1)).save(profile);
        verify(events, times(2)).record(any());
    }

    @Test
    void lateFinalizeCannotChangeDoctorWhoManuallyReturnedAvailable() {
        ConsultationSession session = session();
        session.setDoctorReleasedAt(DUE_AT);
        session.setDoctorReleaseReason(DoctorReleaseReason.SUMMARY_TIMEOUT);
        DoctorCareProfile profile = profile(false);
        profile.setDispatchStatus(DoctorDispatchStatus.AVAILABLE);
        profile.setBusySessionId(null);

        service.onSummaryFinalized(
                session, profile, activeDoctor(), finalized(DUE_AT.plusSeconds(60)), DUE_AT.plusSeconds(60));

        assertEquals(DoctorDispatchStatus.AVAILABLE, profile.getDispatchStatus());
        verify(profiles, never()).save(any());
        verify(applicationEvents, never()).publishEvent(any());
    }

    @Test
    void staleOldSessionCannotReleaseDoctorBusyForNewerSession() {
        ConsultationSession session = session();
        DoctorCareProfile profile = profile(false);
        profile.setBusySessionId(99L);

        service.onSummaryFinalized(
                session, profile, activeDoctor(), finalized(DUE_AT.minusSeconds(1)), DUE_AT.minusSeconds(1));

        assertEquals(DoctorDispatchStatus.BUSY, profile.getDispatchStatus());
        assertEquals(99L, profile.getBusySessionId());
        assertNotNull(session.getDoctorReleasedAt());
        verify(profiles, never()).save(any());
        verify(applicationEvents, never()).publishEvent(any());
    }

    @Test
    void inactiveDoctorNeverBecomesAvailableOnTimelyFinalization() {
        ConsultationSession session = session();
        DoctorCareProfile profile = profile(false);
        UserAccount inactive = activeDoctor();
        inactive.setStatus(AccountStatus.INACTIVE);

        service.onSummaryFinalized(
                session, profile, inactive, finalized(DUE_AT.minusSeconds(1)), DUE_AT.minusSeconds(1));

        assertEquals(DoctorDispatchStatus.UNAVAILABLE, profile.getDispatchStatus());
        assertNull(profile.getBusySessionId());
    }

    @Test
    void restartRecoversFinalizedSummaryUsingPersistedFinalizedTimestamp() {
        ConsultationSession session = session();
        DoctorCareProfile profile = profile(false);
        when(sessions.findByIdForUpdate(50L)).thenReturn(Optional.of(session));
        when(accounts.findByIdForUpdate(20L)).thenReturn(Optional.of(activeDoctor()));
        when(profiles.findByDoctorIdForUpdate(20L)).thenReturn(Optional.of(profile));
        when(summaries.findBySessionIdForUpdate(50L))
                .thenReturn(Optional.of(finalized(DUE_AT.minusSeconds(30))));

        service.processDeadline(50L, DUE_AT.plusSeconds(30));

        assertEquals(FinalSummaryClosureStatus.SUMMARY_FINALIZED, session.getSummaryClosureStatus());
        assertEquals(DoctorDispatchStatus.AVAILABLE, profile.getDispatchStatus());
        assertEquals(DoctorReleaseReason.SUMMARY_FINALIZED, session.getDoctorReleaseReason());
    }

    @Test
    void restartBackfillsMissingDeadlineFromCompletedAtWithoutRestartingTimer() {
        ConsultationSession session = session();
        session.setSummaryDueAt(null);
        DoctorCareProfile profile = profile(false);
        when(sessions.findByIdForUpdate(50L)).thenReturn(Optional.of(session));
        when(accounts.findByIdForUpdate(20L)).thenReturn(Optional.of(activeDoctor()));
        when(profiles.findByDoctorIdForUpdate(20L)).thenReturn(Optional.of(profile));
        when(summaries.findBySessionIdForUpdate(50L)).thenReturn(Optional.empty());

        service.processDeadline(50L, DUE_AT.plusSeconds(5));

        assertEquals(DUE_AT, session.getSummaryDueAt());
        assertEquals(DoctorDispatchStatus.UNAVAILABLE, profile.getDispatchStatus());
        assertEquals(DoctorReleaseReason.SUMMARY_TIMEOUT, session.getDoctorReleaseReason());
    }

    @Test
    void restartRepairsLegacyTwentyFourHourDeadlineAndReleasesOverdueQueueDoctor() {
        ConsultationSession session = session();
        session.setSummaryDueAt(COMPLETED_AT.plusSeconds(24 * 60 * 60));
        DoctorCareProfile profile = profile(false);
        when(sessions.findByIdForUpdate(50L)).thenReturn(Optional.of(session));
        when(accounts.findByIdForUpdate(20L)).thenReturn(Optional.of(activeDoctor()));
        when(profiles.findByDoctorIdForUpdate(20L)).thenReturn(Optional.of(profile));
        when(summaries.findBySessionIdForUpdate(50L)).thenReturn(Optional.empty());

        service.processDeadline(50L, DUE_AT.plusSeconds(5));

        assertEquals(DUE_AT, session.getSummaryDueAt());
        assertEquals(DoctorDispatchStatus.UNAVAILABLE, profile.getDispatchStatus());
        assertNull(profile.getBusySessionId());
        assertEquals(DoctorReleaseReason.SUMMARY_TIMEOUT, session.getDoctorReleaseReason());
    }

    private ConsultationSession session() {
        return ConsultationSession.builder().id(50L).memberId(10L).doctorId(20L)
                .flowType(ConsultationFlowType.QUEUE_DISPATCH_V1)
                .status(ConsultationStatus.COMPLETED).completedAt(COMPLETED_AT)
                .endsAt(COMPLETED_AT).summaryClosureStatus(FinalSummaryClosureStatus.SUMMARY_PENDING)
                .summaryDueAt(DUE_AT).build();
    }

    private DoctorCareProfile profile(boolean stop) {
        return DoctorCareProfile.builder().doctorId(20L).dispatchStatus(DoctorDispatchStatus.BUSY)
                .busySessionId(50L).stopAfterCurrentSession(stop).acceptsOneOnOneCare(true)
                .specialty(DoctorSpecialty.GENERAL_PRACTICE).maxActiveConsultations(1)
                .availabilityJson("{\"weekly\":[]}").timezone("Asia/Ho_Chi_Minh")
                .dispatchStatusChangedAt(COMPLETED_AT).build();
    }

    private UserAccount activeDoctor() {
        return UserAccount.builder().id(20L).email("doctor@example.com").passwordHash("hash")
                .role(UserRole.DOCTOR).status(AccountStatus.ACTIVE).build();
    }

    private ConsultationFinalSummary draft() {
        return ConsultationFinalSummary.builder().id(70L).sessionId(50L).createdByDoctorId(20L)
                .status(ConsultationFinalSummaryStatus.DRAFT).build();
    }

    private ConsultationFinalSummary finalized(Instant at) {
        ConsultationFinalSummary summary = draft();
        summary.setStatus(ConsultationFinalSummaryStatus.FINALIZED);
        summary.setFinalizedAt(at);
        return summary;
    }
}
