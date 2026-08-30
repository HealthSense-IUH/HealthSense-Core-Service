package fit.iuh.se.hschat.service.carehistory.impl;

import fit.iuh.se.hschat.entity.ConsultationFinalSummary;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.ConsultationFinalSummaryStatus;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.repository.ConsultationFinalSummaryRepository;
import fit.iuh.se.hschat.repository.ConsultationFinalSummaryAddendumRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.service.authorization.EpisodeHealthRecordAuthorizationService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CareHistoryServiceImplTest {

    @Mock ConsultationSessionRepository sessionRepository;
    @Mock ConsultationFinalSummaryRepository summaryRepository;
    @Mock ConsultationFinalSummaryAddendumRepository addendumRepository;
    @Mock EpisodeHealthRecordAuthorizationService authorizationService;
    @Mock fit.iuh.se.hsoperations.service.OperationalEventService operationalEventService;

    CareHistoryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CareHistoryServiceImpl(
                sessionRepository, summaryRepository, addendumRepository, authorizationService,
                operationalEventService);
    }

    @Test
    void currentDoctorCanReadOnlyPreviousFinalizedSummaryForContinuity() {
        ConsultationSession current = session(200L, 1L, 20L, ConsultationStatus.ACTIVE);
        ConsultationSession previousA = session(100L, 1L, 10L, ConsultationStatus.COMPLETED);
        ConsultationSession previousDraft = session(101L, 1L, 11L, ConsultationStatus.COMPLETED);
        when(sessionRepository.findByIdAndDoctorId(200L, 20L)).thenReturn(Optional.of(current));
        when(sessionRepository.findByMemberIdAndIdNotAndActivatedAtIsNotNullOrderByStartedAtDesc(1L, 200L))
                .thenReturn(List.of(previousA, previousDraft));
        when(summaryRepository.findBySessionIdInAndStatus(
                List.of(100L, 101L), ConsultationFinalSummaryStatus.FINALIZED))
                .thenReturn(List.of(summary(100L, ConsultationFinalSummaryStatus.FINALIZED)));

        var result = service.getContinuitySummaries(20L, 200L);

        assertEquals(1, result.size());
        assertEquals(100L, result.getFirst().getSessionId());
        assertEquals("finalized", result.getFirst().getFinalizedSummary().getSummary());
        assertEquals(Set.of(501L), result.getFirst().getFinalizedSummary().getReferencedHealthRecordIds());
        verifyNoInteractions(authorizationService);
        verify(operationalEventService).record(argThat(command ->
                command.eventType() == fit.iuh.se.hsoperations.entity.enums.BusinessEventType.CARE_CONTINUITY_ACCESSED
                        && Long.valueOf(20L).equals(command.actorUserId())
                        && Long.valueOf(200L).equals(command.sessionId())));
    }

    @Test
    void continuityIsUnavailableBeforeCurrentSessionActivation() {
        ConsultationSession scheduled = session(200L, 1L, 20L, ConsultationStatus.SCHEDULED);
        scheduled.setActivatedAt(null);
        when(sessionRepository.findByIdAndDoctorId(200L, 20L)).thenReturn(Optional.of(scheduled));

        assertThrows(AppException.class, () -> service.getContinuitySummaries(20L, 200L));
        verify(summaryRepository, never()).findBySessionIdInAndStatus(any(), any());
    }

    @Test
    void memberCareHistoryCannotExposeAnotherMembersEpisode() {
        ConsultationSession otherMember = session(100L, 2L, 10L, ConsultationStatus.COMPLETED);
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(otherMember));

        assertThrows(AppException.class, () -> service.getMemberEpisode(1L, 100L));
        verifyNoInteractions(summaryRepository, authorizationService);
    }

    private ConsultationSession session(Long id, Long memberId, Long doctorId, ConsultationStatus status) {
        return ConsultationSession.builder()
                .id(id).memberId(memberId).doctorId(doctorId).status(status)
                .startedAt(Instant.parse("2026-01-01T00:00:00Z"))
                .activatedAt(Instant.parse("2026-01-01T00:00:00Z"))
                .endsAt(Instant.parse("2026-01-08T00:00:00Z"))
                .build();
    }

    private ConsultationFinalSummary summary(Long sessionId, ConsultationFinalSummaryStatus status) {
        return ConsultationFinalSummary.builder()
                .id(sessionId).sessionId(sessionId).summary("finalized")
                .referencedHealthRecordIds(Set.of(501L))
                .status(status).createdByDoctorId(10L).finalizedAt(Instant.now()).build();
    }
}
