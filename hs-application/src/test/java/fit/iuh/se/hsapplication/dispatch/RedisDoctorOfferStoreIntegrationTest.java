package fit.iuh.se.hsapplication.dispatch;

import fit.iuh.se.hschat.service.dispatch.offer.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "app.consultation.dispatch.scheduler-delay-ms=3600000")
@ActiveProfiles("dev")
class RedisDoctorOfferStoreIntegrationTest {
    @Autowired DoctorOfferStore store;

    @Test void luaOwnershipDeadlinesIdempotentAcceptAndStaleCleanupUseRealRedis() {
        long seed = Math.abs(System.nanoTime());
        long doctorId = seed;
        long queueId = seed + 1;
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        DoctorOffer first = new DoctorOffer("it-" + seed + "-a", queueId, seed + 2, seed + 3, doctorId,
                DoctorOfferState.OFFERED_TO_DOCTOR, now, now.plusSeconds(300), null, null);
        DoctorOffer sameDoctor = new DoctorOffer("it-" + seed + "-b", queueId + 10, seed + 12, seed + 13, doctorId,
                DoctorOfferState.OFFERED_TO_DOCTOR, now, now.plusSeconds(300), null, null);
        DoctorOffer sameQueue = new DoctorOffer("it-" + seed + "-c", queueId, seed + 22, seed + 23, doctorId + 10,
                DoctorOfferState.OFFERED_TO_DOCTOR, now, now.plusSeconds(300), null, null);
        try {
            assertEquals(DoctorOfferStore.CreateResult.CREATED, store.create(first));
            assertEquals(DoctorOfferStore.CreateResult.DOCTOR_ALREADY_HELD, store.create(sameDoctor));
            assertEquals(DoctorOfferStore.CreateResult.QUEUE_ALREADY_HELD, store.create(sameQueue));
            Instant memberDeadline = now.plusSeconds(1200);
            assertEquals(DoctorOfferStore.AcceptResult.ACCEPTED,
                    store.accept(first.offerId(), doctorId, now.plusSeconds(1), memberDeadline));
            assertEquals(DoctorOfferStore.AcceptResult.ALREADY_ACCEPTED,
                    store.accept(first.offerId(), doctorId, now.plusSeconds(2), now.plusSeconds(1300)));
            assertEquals(memberDeadline, store.findById(first.offerId()).orElseThrow().memberConfirmExpiresAt());
            assertTrue(store.heldDoctorIds(List.of(doctorId)).contains(doctorId));
            assertEquals(DoctorOfferStore.ConfirmResult.CONFIRMED,
                    store.confirm(first.offerId(), queueId, seed + 3, doctorId, now.plusSeconds(2)));
            assertEquals(DoctorOfferStore.ConfirmResult.ALREADY_CONFIRMED,
                    store.confirm(first.offerId(), queueId, seed + 3, doctorId, now.plusSeconds(3)));
            DoctorOffer confirmed = store.findById(first.offerId()).orElseThrow();
            assertEquals(DoctorOfferState.CONFIRMED, confirmed.state());
            assertTrue(store.restoreMemberConfirmation(confirmed));
            assertEquals(DoctorOfferState.WAITING_MEMBER_CONFIRMATION,
                    store.findById(first.offerId()).orElseThrow().state());
            assertEquals(DoctorOfferStore.ConfirmResult.EXPIRED,
                    store.confirm(first.offerId(), queueId, seed + 3, doctorId, memberDeadline));

            assertTrue(store.release(first));
            DoctorOffer replacement = new DoctorOffer("it-" + seed + "-replacement", queueId, seed + 2,
                    seed + 3, doctorId, DoctorOfferState.OFFERED_TO_DOCTOR, now, now.minusMillis(1), null, null);
            assertEquals(DoctorOfferStore.CreateResult.CREATED, store.create(replacement));
            assertFalse(store.release(first));
            assertEquals(replacement.offerId(), store.findByDoctorId(doctorId).orElseThrow().offerId());
            assertTrue(store.findById(replacement.offerId()).isPresent(),
                    "Physical TTL must not erase an offer merely because its business deadline passed");
            assertEquals(DoctorOfferStore.AcceptResult.EXPIRED,
                    store.accept(replacement.offerId(), doctorId, now, now.plusSeconds(900)));
            assertTrue(store.dueDoctorOfferIds(now, 1000).contains(replacement.offerId()));
        } finally {
            store.release(first);
            store.findByDoctorId(doctorId).ifPresent(store::release);
            store.release(sameDoctor);
            store.release(sameQueue);
        }
    }
}
