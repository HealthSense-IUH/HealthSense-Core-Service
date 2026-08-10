package fit.iuh.se.hschat.service.finalsummary.impl;

import fit.iuh.se.hschat.dto.request.UpsertConsultationFinalSummaryRequest;
import fit.iuh.se.hschat.dto.response.ConsultationFinalSummaryResponse;
import fit.iuh.se.hschat.entity.ConsultationFinalSummary;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.ConsultationFinalSummaryStatus;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.repository.ConsultationFinalSummaryRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultationFinalSummaryServiceImplTest {

    @Mock
    ConsultationSessionRepository sessionRepository;
    @Mock
    ConsultationFinalSummaryRepository summaryRepository;

    ConsultationFinalSummaryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ConsultationFinalSummaryServiceImpl(sessionRepository, summaryRepository);
    }

    @Test
    void assignedDoctorCanCreateDraftForActiveSession() {
        ConsultationSession session = session(ConsultationStatus.ACTIVE);
        when(sessionRepository.findByIdAndDoctorId(100L, 2L)).thenReturn(Optional.of(session));
        when(summaryRepository.findBySessionId(100L)).thenReturn(Optional.empty());
        when(summaryRepository.save(any(ConsultationFinalSummary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConsultationFinalSummaryResponse response = service.upsertDraft(2L, 100L, request("Care is stable"));

        ArgumentCaptor<ConsultationFinalSummary> captor = ArgumentCaptor.forClass(ConsultationFinalSummary.class);
        verify(summaryRepository).save(captor.capture());
        assertEquals(ConsultationFinalSummaryStatus.DRAFT, captor.getValue().getStatus());
        assertEquals(2L, captor.getValue().getCreatedByDoctorId());
        assertEquals("Care is stable", response.getSummary());
    }

    @Test
    void nonAssignedDoctorCannotCreateDraft() {
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
        when(sessionRepository.findByIdAndDoctorId(100L, 2L)).thenReturn(Optional.of(session(ConsultationStatus.COMPLETED)));
        when(summaryRepository.findBySessionId(100L)).thenReturn(Optional.of(draft));
        when(summaryRepository.save(any(ConsultationFinalSummary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConsultationFinalSummaryResponse response = service.finalizeSummary(2L, 100L);

        assertEquals(ConsultationFinalSummaryStatus.FINALIZED, response.getStatus());
        assertNotNull(response.getFinalizedAt());
        verify(summaryRepository).save(draft);
    }

    @Test
    void finalizedSummaryCannotBeEdited() {
        ConsultationFinalSummary finalized = draft();
        finalized.setStatus(ConsultationFinalSummaryStatus.FINALIZED);
        finalized.setFinalizedAt(Instant.now());
        when(sessionRepository.findByIdAndDoctorId(100L, 2L)).thenReturn(Optional.of(session(ConsultationStatus.COMPLETED)));
        when(summaryRepository.findBySessionId(100L)).thenReturn(Optional.of(finalized));

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
        when(sessionRepository.existsById(100L)).thenReturn(true);
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
}
