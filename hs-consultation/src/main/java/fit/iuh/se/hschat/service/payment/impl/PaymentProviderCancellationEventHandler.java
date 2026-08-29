package fit.iuh.se.hschat.service.payment.impl;

import fit.iuh.se.hschat.entity.ConsultationPayment;
import fit.iuh.se.hschat.entity.enums.PaymentProviderCancellationStatus;
import fit.iuh.se.hschat.event.PaymentProviderCancellationRequestedEvent;
import fit.iuh.se.hschat.repository.ConsultationPaymentRepository;
import fit.iuh.se.hschat.service.payment.PayOSPaymentGateway;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentProviderCancellationEventHandler {
    ConsultationPaymentRepository paymentRepository;
    PayOSPaymentGateway paymentGateway;

    @Async
    @EventListener
    @Transactional
    public void onCancellationRequested(PaymentProviderCancellationRequestedEvent event) {
        paymentRepository.findByRequestIdForUpdate(event.requestId()).stream()
                .filter(payment -> payment.getProviderCancellationStatus()
                        == PaymentProviderCancellationStatus.PENDING)
                .forEach(this::cancelOne);
    }

    private void cancelOne(ConsultationPayment payment) {
        Instant now = Instant.now();
        payment.setProviderCancellationLastAttemptAt(now);
        try {
            paymentGateway.cancelPaymentLink(payment.getOrderCode(),
                    "HealthSense care request cancelled locally");
            payment.setProviderCancellationStatus(PaymentProviderCancellationStatus.SUCCEEDED);
            payment.setProviderCancellationCompletedAt(now);
            payment.setProviderCancellationError(null);
        } catch (Exception exception) {
            log.warn("Provider cancellation failed for payment {}: {}", payment.getId(), exception.getMessage());
            payment.setProviderCancellationStatus(PaymentProviderCancellationStatus.FAILED);
            payment.setProviderCancellationError(truncate(exception.getMessage()));
        }
        paymentRepository.save(payment);
    }

    private String truncate(String value) {
        if (value == null) return "Unknown provider cancellation failure";
        return value.length() <= 1000 ? value : value.substring(0, 1000);
    }
}
