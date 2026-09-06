package fit.iuh.se.hsapplication.scheduler;

import fit.iuh.se.hschat.service.dispatch.DoctorOfferService;
import fit.iuh.se.hschat.service.dispatch.offer.DoctorOfferStore;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ConsultationDispatchSchedulerTest {

    private final DoctorOfferService offerService = mock(DoctorOfferService.class);
    private final DoctorOfferStore offerStore = mock(DoctorOfferStore.class);
    private final ConsultationDispatchScheduler scheduler =
            new ConsultationDispatchScheduler(offerService, offerStore);

    @Test
    void frequentDeadlineScanDoesNotPollDatabaseMaintenance() {
        when(offerStore.dueDoctorOfferIds(any(Instant.class), eq(100))).thenReturn(List.of("doctor-expired"));
        when(offerStore.dueMemberConfirmationOfferIds(any(Instant.class), eq(100)))
                .thenReturn(List.of("member-expired"));

        scheduler.processDeadlines();

        verify(offerService).processDoctorTimeout("doctor-expired");
        verify(offerService).processMemberConfirmationTimeout("member-expired");
        verify(offerService, never()).reconcileMissingOffers();
        verify(offerService, never()).dispatchOne();
    }

    @Test
    void maintenanceReconcilesAndProvidesDispatchSafetyNet() {
        scheduler.maintainQueue();

        verify(offerService).reconcileMissingOffers();
        verify(offerService).dispatchOne();
        verifyNoInteractions(offerStore);
    }
}
