package fit.iuh.se.hschat.service.message.impl;

import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.*;
import fit.iuh.se.hschat.service.continuation.ContinuationState;
import fit.iuh.se.hschat.service.continuation.ContinuationStore;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SnapshotSupportHoursPolicyTest {
    private final ContinuationStore store = mock(ContinuationStore.class);
    private final SnapshotSupportHoursPolicy policy = new SnapshotSupportHoursPolicy(store);

    @Test void activeQueueSessionAllowsBothParticipantsInsideInitialBlock() {
        ConsultationSession session = queueSession(Instant.now().plusSeconds(60));
        assertTrue(policy.canSendNow(session, ConsultationParticipantRole.MEMBER));
        assertTrue(policy.canSendNow(session, ConsultationParticipantRole.DOCTOR));
    }

    @Test void queueSessionChatClosesAtExpiredBlockRegardlessOfRole() {
        Instant now = Instant.parse("2026-09-06T03:00:00Z");
        policy.clock = Clock.fixed(now, ZoneOffset.UTC);
        ConsultationSession session = queueSession(now.minusSeconds(301));
        assertFalse(policy.canSendNow(session, ConsultationParticipantRole.MEMBER));
        assertFalse(policy.canSendNow(session, ConsultationParticipantRole.DOCTOR));
    }

    @Test void queueSessionChatRemainsWritableDuringOpenedGrace() {
        Instant now = Instant.parse("2026-09-06T03:00:00Z");
        Instant endedAt = now.minusSeconds(60);
        policy.clock = Clock.fixed(now, ZoneOffset.UTC);
        ConsultationSession session = queueSession(endedAt);
        when(store.find(1L, 0)).thenReturn(Optional.of(new ContinuationState(1L, 0,
                ContinuationDecision.PENDING, ContinuationDecision.PENDING,
                endedAt, endedAt.plusSeconds(300))));

        assertTrue(policy.canSendNow(session, ConsultationParticipantRole.MEMBER));
        assertTrue(policy.canSendNow(session, ConsultationParticipantRole.DOCTOR));
    }

    @Test void queueSessionChatFailsClosedWhenGraceStateIsMissing() {
        Instant now = Instant.parse("2026-09-06T03:00:00Z");
        policy.clock = Clock.fixed(now, ZoneOffset.UTC);
        ConsultationSession session = queueSession(now.minusSeconds(60));
        when(store.find(1L, 0)).thenReturn(Optional.empty());

        assertFalse(policy.canSendNow(session, ConsultationParticipantRole.MEMBER));
    }

    private ConsultationSession queueSession(Instant endsAt) {
        return ConsultationSession.builder().id(1L).flowType(ConsultationFlowType.QUEUE_DISPATCH_V1)
                .status(ConsultationStatus.ACTIVE).endsAt(endsAt).build();
    }
}
