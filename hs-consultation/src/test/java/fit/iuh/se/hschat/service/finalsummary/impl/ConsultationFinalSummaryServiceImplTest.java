package fit.iuh.se.hschat.service.finalsummary.impl;

import fit.iuh.se.hschat.dto.request.UpsertConsultationFinalSummaryRequest;
import fit.iuh.se.hschat.dto.request.CreateFinalSummaryAddendumRequest;
import fit.iuh.se.hschat.dto.response.ConsultationFinalSummaryResponse;
import fit.iuh.se.hschat.entity.ConsultationFinalSummary;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.ConsultationFinalSummaryAddendum;
import fit.iuh.se.hschat.entity.enums.ConsultationFinalSummaryStatus;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.repository.ConsultationFinalSummaryRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.repository.ConsultationFinalSummaryAddendumRepository;
import fit.iuh.se.hschat.repository.EpisodeHealthRecordAuthorizationRepository;
import fit.iuh.se.hschat.service.finalsummary.FinalSummaryClosureService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultationFinalSummaryServiceImplTest {

    @Mock
    ConsultationSessionRepository sessionRepository;
    @Mock
    ConsultationFinalSummaryRepository summaryRepository;
    @Mock
    ConsultationFinalSummaryAddendumRepository addendumRepository;
    @Mock
    EpisodeHealthRecordAuthorizationRepository authorizationRepository;
    @Mock
    UserAccountRepository userAccountRepository;
    @Mock
    FinalSummaryClosureService closureService;
    @Mock
    fit.iuh.se.hsoperations.service.OperationalEventService operationalEventService;

    ConsultationFinalSummaryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ConsultationFinalSummaryServiceImpl(
                sessionRepository,
                summaryRepository,
                addendumRepository,
                authorizationRepository,
                userAccountRepository,
                closureService,
                operationalEventService);
        lenient().when(userAccountRepository.findById(2L)).thenReturn(Optional.of(user(2L, UserRole.DOCTOR, AccountStatus.ACTIVE)));
    }

    @Test
    void assignedDoctorCanCreateDraftForActiveSession() {
        ConsultationSession session = session(ConsultationStatus.ACTIVE);
        when(sessionRepository.findByIdAndDoctorId(100L, 2L)).thenReturn(Optional.of(session));
        when(summaryRepository.findBySessionIdForUpdate(100L)).thenReturn(Optional.empty());
        when(summaryRepository.save(any(ConsultationFinalSummary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConsultationFinalSummaryResponse response = service.upsertDraft(2L, 100L, request("Care is stable"));

        ArgumentCaptor<ConsultationFinalSummary> captor = ArgumentCaptor.forClass(ConsultationFinalSummary.class);
        verify(summaryRepository).save(captor.capture());
        assertEquals(ConsultationFinalSummaryStatus.DRAFT, captor.getValue().getStatus());
        assertEquals(2L, captor.getValue().getCreatedByDoctorId());
        assertEquals("Care is stable", response.getSummary());
    }

    @Test
    void draftMayRemainIncompleteUntilExplicitFinalization() {
        ConsultationSession session = session(ConsultationStatus.ACTIVE);
        when(sessionRepository.findByIdAndDoctorId(100L, 2L)).thenReturn(Optional.of(session));
        when(summaryRepository.findBySessionIdForUpdate(100L)).thenReturn(Optional.empty());
        when(summaryRepository.save(any(ConsultationFinalSummary.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.upsertDraft(2L, 100L,
                new UpsertConsultationFinalSummaryRequest(null, null, null, null));

        assertEquals(ConsultationFinalSummaryStatus.DRAFT, response.getStatus());
        assertNull(response.getSummary());
    }

    @Test
    void nonAssignedDoctorCannotCreateDraft() {
        when(userAccountRepository.findById(9L))
                .thenReturn(Optional.of(user(9L, UserRole.DOCTOR, AccountStatus.ACTIVE)));
        when(sessionRepository.findByIdAndDoctorId(100L, 9L)).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> service.upsertDraft(9L, 100L, request("No access")));
        verify(summaryRepository, never()).save(any());
    }

    @Test
    void cannotCreateDraftForScheduledSession() {
        when(sessionRepository.findByIdAndDoctorId(100L, 2L)).thenReturn(Optional.of(session(ConsultationStatus.SCHEDULED)));

        assertThrows(AppException.class, () -> service.upsertDraft(2L, 100L, request("Too early")));
        verify(summaryRepository, never()).save(any());
    }

    @Test
    void canFinalizeOnlyWhenSessionCompleted() {
        when(sessionRepository.findByIdAndDoctorId(100L, 2L)).thenReturn(Optional.of(session(ConsultationStatus.ACTIVE)));

        assertThrows(AppException.class, () -> service.finalizeSummary(2L, 100L));
        verify(summaryRepository, never()).save(any());
    }

    @Test
    void finalizeLocksDraftForCompletedSession() {
        ConsultationFinalSummary draft = draft();
        ConsultationSession completed = session(ConsultationStatus.COMPLETED);
        when(sessionRepository.findByIdAndDoctorId(100L, 2L)).thenReturn(Optional.of(completed));
        when(summaryRepository.findBySessionIdForUpdate(100L)).thenReturn(Optional.of(draft));
        when(summaryRepository.save(any(ConsultationFinalSummary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConsultationFinalSummaryResponse response = service.finalizeSummary(2L, 100L);

        assertEquals(ConsultationFinalSummaryStatus.FINALIZED, response.getStatus());
        assertNotNull(response.getFinalizedAt());
        verify(summaryRepository).save(draft);
        verify(closureService).onSummaryFinalized(eq(completed), any(Instant.class));
        verify(operationalEventService).record(argThat(command ->
                command.eventType() == fit.iuh.se.hsoperations.entity.enums.BusinessEventType.FINAL_SUMMARY_FINALIZED
                        && Long.valueOf(2L).equals(command.actorUserId())));
    }

    @Test
    void finalizeRejectsBlankSummary() {
        assertRequiredFieldRejected(" ", "Observed", "Recommend");
    }

    @Test
    void finalizeRejectsBlankObservations() {
        assertRequiredFieldRejected("Summary", " ", "Recommend");
    }

    @Test
    void finalizeRejectsBlankRecommendations() {
        assertRequiredFieldRejected("Summary", "Observed", null);
    }

    @Test
    void followUpRecommendationIsOptionalAtFinalization() {
        ConsultationFinalSummary draft = draft();
        draft.setFollowUpRecommendation(null);
        when(sessionRepository.findByIdAndDoctorId(100L, 2L))
                .thenReturn(Optional.of(session(ConsultationStatus.COMPLETED)));
        when(summaryRepository.findBySessionIdForUpdate(100L)).thenReturn(Optional.of(draft));
        when(summaryRepository.save(draft)).thenReturn(draft);

        assertEquals(ConsultationFinalSummaryStatus.FINALIZED,
                service.finalizeSummary(2L, 100L).getStatus());
    }

    @Test
    void assignedDoctorCanCompleteDraftAfterSessionCompletion() {
        ConsultationFinalSummary draft = draft();
        when(sessionRepository.findByIdAndDoctorId(100L, 2L))
                .thenReturn(Optional.of(session(ConsultationStatus.COMPLETED)));
        when(summaryRepository.findBySessionIdForUpdate(100L)).thenReturn(Optional.of(draft));
        when(summaryRepository.save(draft)).thenReturn(draft);

        var response = service.upsertDraft(2L, 100L, request("Updated after care ended"));

        assertEquals("Updated after care ended", response.getSummary());
        assertEquals(ConsultationFinalSummaryStatus.DRAFT, response.getStatus());
    }

    @Test
    void addendumPreservesFinalizedOriginal() {
        ConsultationFinalSummary finalized = draft();
        finalized.setStatus(ConsultationFinalSummaryStatus.FINALIZED);
        finalized.setFinalizedAt(Instant.now());
        when(sessionRepository.findByIdAndDoctorId(100L, 2L))
                .thenReturn(Optional.of(session(ConsultationStatus.COMPLETED)));
        when(summaryRepository.findBySessionIdForUpdate(100L)).thenReturn(Optional.of(finalized));
        when(addendumRepository.save(any())).thenAnswer(invocation -> {
            ConsultationFinalSummaryAddendum value = invocation.getArgument(0);
            value.setId(77L);
            return value;
        });

        var result = service.createAddendum(2L, 100L,
                new CreateFinalSummaryAddendumRequest("Clarification", "Corrected follow-up interval"));

        assertEquals("Care is stable", finalized.getSummary());
        assertEquals(ConsultationFinalSummaryStatus.FINALIZED, finalized.getStatus());
        assertEquals(10L, result.getSummaryId());
        verify(summaryRepository, never()).save(finalized);
        verify(operationalEventService).record(argThat(command ->
                command.eventType() == fit.iuh.se.hsoperations.entity.enums.BusinessEventType.FINAL_SUMMARY_ADDENDUM_CREATED
                        && Long.valueOf(100L).equals(command.sessionId())));
    }

    @Test
    void draftMayReferenceOnlyEpisodeAuthorizedHealthRecords() {
        UpsertConsultationFinalSummaryRequest request = request("Care is stable");
        request.setReferencedHealthRecordIds(Set.of(501L, 999L));
        when(sessionRepository.findByIdAndDoctorId(100L, 2L))
                .thenReturn(Optional.of(session(ConsultationStatus.ACTIVE)));
        when(summaryRepository.findBySessionIdForUpdate(100L)).thenReturn(Optional.empty());
        when(authorizationRepository.existsBySessionIdAndHealthRecordId(eq(100L), anyLong()))
                .thenAnswer(invocation -> Long.valueOf(501L).equals(invocation.getArgument(1)));

        assertThrows(AppException.class, () -> service.upsertDraft(2L, 100L, request));
        verify(summaryRepository, never()).save(any());
    }

    @Test
    void disabledDoctorCannotFinalizeOrBeImpersonated() {
        when(userAccountRepository.findById(2L))
                .thenReturn(Optional.of(user(2L, UserRole.DOCTOR, AccountStatus.INACTIVE)));

        assertThrows(AppException.class, () -> service.finalizeSummary(2L, 100L));
        verify(summaryRepository, never()).save(any());
    }

    @Test
    void administratorCannotFinalizeAsDoctor() {
        when(userAccountRepository.findById(9L))
                .thenReturn(Optional.of(user(9L, UserRole.ADMIN, AccountStatus.ACTIVE)));

        assertThrows(AppException.class, () -> service.finalizeSummary(9L, 100L));
        verify(summaryRepository, never()).save(any());
    }

    @Test
    void finalizedSummaryCannotBeEdited() {
        ConsultationFinalSummary finalized = draft();
        finalized.setStatus(ConsultationFinalSummaryStatus.FINALIZED);
        finalized.setFinalizedAt(Instant.now());
        when(sessionRepository.findByIdAndDoctorId(100L, 2L)).thenReturn(Optional.of(session(ConsultationStatus.COMPLETED)));
        when(summaryRepository.findBySessionIdForUpdate(100L)).thenReturn(Optional.of(finalized));

        assertThrows(AppException.class, () -> service.upsertDraft(2L, 100L, request("Changed")));
        verify(summaryRepository, never()).save(any());
    }

    @Test
    void memberCanReadOnlyFinalizedSummary() {
        ConsultationFinalSummary finalized = draft();
        finalized.setStatus(ConsultationFinalSummaryStatus.FINALIZED);
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(session(ConsultationStatus.COMPLETED)));
        when(summaryRepository.findBySessionId(100L)).thenReturn(Optional.of(finalized));

        ConsultationFinalSummaryResponse response = service.getForMember(1L, 100L);

        assertEquals(ConsultationFinalSummaryStatus.FINALIZED, response.getStatus());
    }

    @Test
    void memberCannotReadDraftSummary() {
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(session(ConsultationStatus.COMPLETED)));
        when(summaryRepository.findBySessionId(100L)).thenReturn(Optional.of(draft()));

        assertThrows(AppException.class, () -> service.getForMember(1L, 100L));
    }

    @Test
    void adminCanReadOnlyFinalizedSummary() {
        ConsultationFinalSummary finalized = draft();
        finalized.setStatus(ConsultationFinalSummaryStatus.FINALIZED);
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(session(ConsultationStatus.COMPLETED)));
        when(summaryRepository.findBySessionId(100L)).thenReturn(Optional.of(finalized));

        ConsultationFinalSummaryResponse response = service.getForAdmin(UserRole.CARE_COORDINATOR, 100L);

        assertEquals(ConsultationFinalSummaryStatus.FINALIZED, response.getStatus());
    }

    private ConsultationSession session(ConsultationStatus status) {
        return ConsultationSession.builder()
                .id(100L)
                .memberId(1L)
                .doctorId(2L)
                .status(status)
                .startedAt(Instant.parse("2026-08-10T08:00:00Z"))
                .endsAt(Instant.parse("2026-08-17T08:00:00Z"))
                .build();
    }

    private ConsultationFinalSummary draft() {
        return ConsultationFinalSummary.builder()
                .id(10L)
                .sessionId(100L)
                .createdByDoctorId(2L)
                .status(ConsultationFinalSummaryStatus.DRAFT)
                .summary("Care is stable")
                .observations("Observed stable vitals")
                .recommendations("Keep daily monitoring")
                .build();
    }

    private UpsertConsultationFinalSummaryRequest request(String summary) {
        return new UpsertConsultationFinalSummaryRequest(
                summary,
                "Observed stable vitals",
                "Keep daily monitoring",
                "Follow up if symptoms change"
        );
    }

    private void assertRequiredFieldRejected(String summary, String observations, String recommendations) {
        ConsultationFinalSummary draft = draft();
        draft.setSummary(summary);
        draft.setObservations(observations);
        draft.setRecommendations(recommendations);
        when(sessionRepository.findByIdAndDoctorId(100L, 2L))
                .thenReturn(Optional.of(session(ConsultationStatus.COMPLETED)));
        when(summaryRepository.findBySessionIdForUpdate(100L)).thenReturn(Optional.of(draft));

        assertThrows(AppException.class, () -> service.finalizeSummary(2L, 100L));
        verify(summaryRepository, never()).save(any());
    }

    private UserAccount user(Long id, UserRole role, AccountStatus status) {
        return UserAccount.builder().id(id).email(id + "@example.com").passwordHash("hash")
                .role(role).status(status).build();
    }
}
