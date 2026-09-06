package fit.iuh.se.hschat.service.dispatch.impl;

import fit.iuh.se.hschat.entity.enums.DoctorDispatchStatus;
import fit.iuh.se.hschat.service.dispatch.DoctorOfferService;
import fit.iuh.se.hschat.service.dispatch.event.DispatchRequested;
import fit.iuh.se.hschat.service.dispatch.event.DoctorDispatchStatusChanged;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class DispatchTriggerListener {
    private final DoctorOfferService offerService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(DispatchRequested event) { attempt(event.reason()); }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(DoctorDispatchStatusChanged event) {
        if (event.currentStatus() == DoctorDispatchStatus.AVAILABLE) attempt("doctor-available");
    }

    private void attempt(String reason) {
        try { offerService.dispatchOne(); }
        catch (RuntimeException ex) { log.warn("Dispatch trigger {} will be retried by scheduler", reason, ex); }
    }
}
