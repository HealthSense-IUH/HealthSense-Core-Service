package fit.iuh.se.hschat.service.finalsummary.impl;

import fit.iuh.se.hschat.entity.ConsultationFinalSummary;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.ConsultationFinalSummaryStatus;
import fit.iuh.se.hschat.entity.enums.FinalSummaryClosureStatus;
import fit.iuh.se.hschat.repository.ConsultationFinalSummaryRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinalSummaryClosureServiceImplTest {

    @Mock ConsultationSessionRepository sessionRepository;
    @Mock ConsultationFinalSummaryRepository summaryRepository;
    @Mock UserAccountRepository userAccountRepository;

    FinalSummaryClosureServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new FinalSummaryClosureServiceImpl(
                sessionRepository, summaryRepository, userAccountRepository);
        ReflectionTestUtils.setField(service, "summaryDueHours", 24L);
    }

    @Test
    void completedEpisodeWithoutSummaryGetsPendingObligation() {
        ConsultationSession session = session();
        Instant completedAt = Instant.parse("2026-08-28T10:00:00Z");
        when(summaryRepository.findBySessionId(100L)).thenReturn(Optional.empty());
        activeDoctor();

        service.onSessionCompleted(session, completedAt);

        assertEquals(FinalSummaryClosureStatus.SUMMARY_PENDING, session.getSummaryClosureStatus());
        assertEquals(completedAt.plusSeconds(24 * 3600), session.getSummaryDueAt());
    }

    @Test
    void completedEpisodeWithDraftAlsoGetsPendingObligation() {
        ConsultationSession session = session();
        when(summaryRepository.findBySessionId(100L)).thenReturn(Optional.of(
                ConsultationFinalSummary.builder().status(ConsultationFinalSummaryStatus.DRAFT).build()));
        activeDoctor();

        service.onSessionCompleted(session, Instant.now());

        assertEquals(FinalSummaryClosureStatus.SUMMARY_PENDING, session.getSummaryClosureStatus());
    }

    @Test
    void pendingObligationBecomesOverdueAfterDueAt() {
        ConsultationSession session = session();
        session.setSummaryClosureStatus(FinalSummaryClosureStatus.SUMMARY_PENDING);
        session.setSummaryDueAt(Instant.parse("2026-08-28T10:00:00Z"));
        when(sessionRepository.findBySummaryClosureStatusIn(any())).thenReturn(List.of(session));
        activeDoctor();

        service.refreshOpenClosures(Instant.parse("2026-08-28T10:00:01Z"));

        assertEquals(FinalSummaryClosureStatus.SUMMARY_OVERDUE, session.getSummaryClosureStatus());
        verify(sessionRepository).save(session);
    }

    @Test
    void disabledDoctorCreatesEscalationWithoutClinicalAuthorship() {
        ConsultationSession session = session();
        when(summaryRepository.findBySessionId(100L)).thenReturn(Optional.empty());
        when(userAccountRepository.findById(2L)).thenReturn(Optional.of(
                user(AccountStatus.INACTIVE)));

        service.onSessionCompleted(session, Instant.parse("2026-08-28T10:00:00Z"));

        assertEquals(FinalSummaryClosureStatus.ESCALATED, session.getSummaryClosureStatus());
        assertNotNull(session.getSummaryEscalatedAt());
        assertTrue(session.getSummaryEscalationReason().contains("not active"));
        verify(summaryRepository).findBySessionId(100L);
    }

    @Test
    void finalizationClearsOperationalEscalation() {
        ConsultationSession session = session();
        session.setSummaryClosureStatus(FinalSummaryClosureStatus.ESCALATED);
        session.setSummaryEscalatedAt(Instant.now());
        session.setSummaryEscalationReason("old");

        service.onSummaryFinalized(session, Instant.now());

        assertEquals(FinalSummaryClosureStatus.SUMMARY_FINALIZED, session.getSummaryClosureStatus());
        assertNull(session.getSummaryEscalatedAt());
        assertNull(session.getSummaryEscalationReason());
    }

    private void activeDoctor() {
        when(userAccountRepository.findById(2L)).thenReturn(Optional.of(user(AccountStatus.ACTIVE)));
    }

    private UserAccount user(AccountStatus status) {
        return UserAccount.builder().id(2L).email("doctor@example.com").passwordHash("hash")
                .role(UserRole.DOCTOR).status(status).build();
    }

    private ConsultationSession session() {
        return ConsultationSession.builder().id(100L).doctorId(2L).build();
    }
}
