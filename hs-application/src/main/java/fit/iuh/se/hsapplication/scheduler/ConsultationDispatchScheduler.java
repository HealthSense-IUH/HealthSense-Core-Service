package fit.iuh.se.hsapplication.scheduler;

import fit.iuh.se.hschat.service.dispatch.DoctorOfferService;
import fit.iuh.se.hschat.service.dispatch.offer.DoctorOfferStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConsultationDispatchScheduler {
    private final DoctorOfferService offerService;
    private final DoctorOfferStore offerStore;

    @Scheduled(fixedDelayString = "${app.consultation.dispatch.scheduler-delay-ms:1000}")
    public void processDeadlines() {
        try {
            offerStore.dueDoctorOfferIds(java.time.Instant.now(), 100)
                    .forEach(offerService::processDoctorTimeout);
            offerStore.dueMemberConfirmationOfferIds(java.time.Instant.now(), 100)
                    .forEach(offerService::processMemberConfirmationTimeout);
        } catch (RuntimeException ex) {
            log.warn("Queue dispatch deadline scan failed closed; a later tick will retry", ex);
        }
    }

    @Scheduled(fixedDelayString = "${app.consultation.dispatch.maintenance-delay-ms:30000}")
    public void maintainQueue() {
        try {
            offerService.reconcileMissingOffers();
            offerService.dispatchOne();
        } catch (RuntimeException ex) {
            log.warn("Queue dispatch maintenance failed closed; a later tick will retry", ex);
        }
    }
}
