package fit.iuh.se.hschat.service.continuation.impl;

import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.DoctorCareProfile;
import fit.iuh.se.hschat.entity.enums.*;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.service.continuation.ContinuationStore;
import fit.iuh.se.hsoperations.event.OperationalEventPublisher;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class QueueSessionCompletionServiceImplTest {

    @Test
    void completionIsIdempotentInitializesSummaryAndCannotReleaseDoctor() {
        ConsultationSessionRepository sessions = mock(ConsultationSessionRepository.class);
        ContinuationStore store = mock(ContinuationStore.class);
        OperationalEventPublisher events = mock(OperationalEventPublisher.class);
        QueueSessionCompletionServiceImpl service =
                new QueueSessionCompletionServiceImpl(sessions, store, events);
        Instant now = Instant.parse("2026-09-06T03:20:00Z");
        ConsultationSession session = ConsultationSession.builder().id(50L).memberId(10L).doctorId(20L)
                .flowType(ConsultationFlowType.QUEUE_DISPATCH_V1).status(ConsultationStatus.ACTIVE)
                .continuationRound(0).blockStartedAt(now.minusSeconds(1200)).endsAt(now.minusSeconds(300))
                .build();
        DoctorCareProfile doctor = DoctorCareProfile.builder().doctorId(20L)
                .dispatchStatus(DoctorDispatchStatus.BUSY).busySessionId(50L).build();
        when(sessions.findByIdForUpdate(50L)).thenReturn(Optional.of(session));
        when(store.find(50L, 0)).thenReturn(Optional.empty());

        service.complete(50L, 0, now, ConsultationCompletionReason.CONTINUATION_GRACE_EXPIRED);

        assertEquals(ConsultationStatus.COMPLETED, session.getStatus());
        assertEquals(now, session.getCompletedAt());
        assertEquals(FinalSummaryClosureStatus.SUMMARY_PENDING, session.getSummaryClosureStatus());
        assertEquals(now.plusSeconds(600), session.getSummaryDueAt());
        assertEquals(DoctorDispatchStatus.BUSY, doctor.getDispatchStatus());
        assertEquals(50L, doctor.getBusySessionId());
        verify(sessions).save(session);
        verify(events, times(2)).record(any());
        verify(events).record(argThat(command -> command.eventType()
                == fit.iuh.se.hsoperations.entity.enums.BusinessEventType.SUMMARY_PENDING
                && command.needsAction() == null
                && command.notifications().size() == 1
                && command.notifications().getFirst().recipientId().equals(20L)));
        verify(store).release(50L, 0);

        service.complete(50L, 0, now.plusSeconds(1),
                ConsultationCompletionReason.CONTINUATION_GRACE_EXPIRED);
        assertEquals(now.plusSeconds(600), session.getSummaryDueAt());
        verify(sessions, times(1)).save(any());
        verify(events, times(2)).record(any());
    }
}
