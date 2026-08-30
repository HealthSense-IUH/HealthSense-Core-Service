package fit.iuh.se.hschat.service.payment.impl;

import fit.iuh.se.hschat.entity.ConsultationPayment;
import fit.iuh.se.hschat.entity.enums.PaymentProviderCancellationStatus;
import fit.iuh.se.hschat.event.PaymentProviderCancellationRequestedEvent;
import fit.iuh.se.hschat.repository.ConsultationPaymentRepository;
import fit.iuh.se.hschat.service.payment.PayOSPaymentGateway;
import fit.iuh.se.hsoperations.dto.command.*;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.service.OperationalEventService;
import fit.iuh.se.hsuser.entity.enums.UserRole;
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
    OperationalEventService operationalEventService;

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
        recordResult(payment, null, null);
    }

    private void recordResult(ConsultationPayment payment, Long actorId, UserRole role) {
        boolean failed = payment.getProviderCancellationStatus() == PaymentProviderCancellationStatus.FAILED;
        String actionKey = "payment:" + payment.getId() + ":provider-cancellation-failure";
        if (!failed) operationalEventService.resolveNeedsAction(actionKey, "Provider payment link cancellation succeeded");
        operationalEventService.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.PAYMENT).domainId(payment.getId())
                .eventType(failed ? BusinessEventType.PAYMENT_PROVIDER_CANCELLATION_FAILED
                        : BusinessEventType.PAYMENT_PROVIDER_CANCELLATION_SUCCEEDED)
                .actorType(actorId == null ? BusinessActorType.SYSTEM : BusinessActorType.USER)
                .actorUserId(actorId).actorRole(role == null ? null : role.name())
                .requestId(payment.getRequestId()).paymentId(payment.getId()).memberId(payment.getMemberId())
                .newState(payment.getProviderCancellationStatus().name()).reason(payment.getProviderCancellationError())
                .idempotencyKey("payment:" + payment.getId() + ":provider-cancellation:"
                        + payment.getProviderCancellationStatus() + ":" + payment.getProviderCancellationLastAttemptAt())
                .needsAction(failed ? new NeedsActionIntent(NeedsActionType.PROVIDER_CANCELLATION_RECONCILIATION,
                        NeedsActionPriority.HIGH, "Provider cancellation reconciliation",
                        "The local request is cancelled but the provider payment link cancellation failed.",
                        BusinessDomainType.PAYMENT, payment.getId(), UserRole.ADMIN.name(), actionKey) : null)
                .build());
    }

    private String truncate(String value) {
        if (value == null) return "Unknown provider cancellation failure";
        return value.length() <= 1000 ? value : value.substring(0, 1000);
    }
}
