package fit.iuh.se.hsapplication.scheduler;

import fit.iuh.se.hschat.service.payment.ConsultationPaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConsultationPaymentExpirationScheduler {

    ConsultationPaymentService consultationPaymentService;

    @Scheduled(fixedDelayString = "${app.payment.expiration-fixed-delay-ms:60000}")
    public void expireOverduePayments() {
        try {
            consultationPaymentService.expireOverduePayments();
        } catch (RuntimeException exception) {
            log.warn("Consultation payment expiration job failed: {}", exception.getMessage());
        }
    }
}
