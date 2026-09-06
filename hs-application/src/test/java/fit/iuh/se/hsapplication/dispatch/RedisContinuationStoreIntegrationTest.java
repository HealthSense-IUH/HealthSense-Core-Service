package fit.iuh.se.hsapplication.dispatch;

import fit.iuh.se.hschat.entity.enums.ContinuationDecision;
import fit.iuh.se.hschat.service.continuation.ContinuationState;
import fit.iuh.se.hschat.service.continuation.ContinuationStore;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "app.consultation.dispatch.scheduler-delay-ms=3600000",
        "app.consultation.continuation.scheduler-delay-ms=3600000"
})
@ActiveProfiles("dev")
class RedisContinuationStoreIntegrationTest {

    @Autowired ContinuationStore store;

    @Test
    void luaStateIsAtomicImmutableDeadlineAwareAndOwnershipSafeOnRealRedis() {
        long sessionId = Math.abs(System.nanoTime());
        int round = 0;
        Instant promptedAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        Instant deadline = promptedAt.plusSeconds(300);
        try {
            assertEquals(ContinuationStore.OpenResult.CREATED,
                    store.open(sessionId, round, promptedAt, deadline));
            assertEquals(ContinuationStore.OpenResult.ALREADY_OPEN,
                    store.open(sessionId, round, promptedAt.plusSeconds(1), deadline.plusSeconds(1)));
            ContinuationState initial = store.find(sessionId, round).orElseThrow();
            assertEquals(promptedAt, initial.promptedAt());
            assertEquals(deadline, initial.graceExpiresAt());

            assertEquals(ContinuationStore.DecisionResult.ACCEPTED,
                    store.decide(sessionId, round, UserRole.MEMBER,
                            ContinuationDecision.CONTINUE, promptedAt.plusSeconds(1)).result());
            assertEquals(ContinuationStore.DecisionResult.IDEMPOTENT,
                    store.decide(sessionId, round, UserRole.MEMBER,
                            ContinuationDecision.CONTINUE, promptedAt.plusSeconds(2)).result());
            assertEquals(ContinuationStore.DecisionResult.CONFLICT,
                    store.decide(sessionId, round, UserRole.MEMBER,
                            ContinuationDecision.STOP, promptedAt.plusSeconds(3)).result());
            assertEquals(ContinuationStore.DecisionResult.EXPIRED,
                    store.decide(sessionId, round, UserRole.DOCTOR,
                            ContinuationDecision.CONTINUE, deadline).result());

            assertEquals(ContinuationStore.OpenResult.CREATED,
                    store.open(sessionId, round + 1, deadline, deadline.plusSeconds(300)));
            assertTrue(store.release(sessionId, round));
            assertTrue(store.find(sessionId, round + 1).isPresent(),
                    "Old-round cleanup must not alter a newer-round key");
        } finally {
            store.release(sessionId, round);
            store.release(sessionId, round + 1);
        }
    }

    @Test
    void simultaneousParticipantContinuesConvergeToOneBothContinueState() {
        long sessionId = Math.abs(System.nanoTime());
        Instant promptedAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        Instant deadline = promptedAt.plusSeconds(300);
        try {
            assertEquals(ContinuationStore.OpenResult.CREATED,
                    store.open(sessionId, 0, promptedAt, deadline));
            CompletableFuture<ContinuationStore.DecisionOutcome> member = CompletableFuture.supplyAsync(() ->
                    store.decide(sessionId, 0, UserRole.MEMBER,
                            ContinuationDecision.CONTINUE, promptedAt.plusSeconds(1)));
            CompletableFuture<ContinuationStore.DecisionOutcome> doctor = CompletableFuture.supplyAsync(() ->
                    store.decide(sessionId, 0, UserRole.DOCTOR,
                            ContinuationDecision.CONTINUE, promptedAt.plusSeconds(1)));
            CompletableFuture.allOf(member, doctor).join();

            assertEquals(ContinuationStore.DecisionResult.ACCEPTED, member.join().result());
            assertEquals(ContinuationStore.DecisionResult.ACCEPTED, doctor.join().result());
            assertTrue(store.find(sessionId, 0).orElseThrow().bothContinue());
        } finally {
            store.release(sessionId, 0);
        }
    }
}
