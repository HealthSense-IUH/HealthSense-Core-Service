package fit.iuh.se.hschat.service.payment.impl;

import fit.iuh.se.hschat.entity.ConsultationPayment;
import fit.iuh.se.hschat.event.PaymentProviderCancellationRequestedEvent;
import fit.iuh.se.hschat.entity.enums.ConsultationPaymentStatus;
import fit.iuh.se.hschat.entity.enums.PaymentProviderCancellationStatus;
import fit.iuh.se.hschat.repository.ConsultationPaymentRepository;
import fit.iuh.se.hschat.service.payment.PayOSPaymentGateway;
import fit.iuh.se.hschat.service.payment.PaymentCancellationService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentCancellationServiceImpl implements PaymentCancellationService {

    ConsultationPaymentRepository paymentRepository;
    PayOSPaymentGateway paymentGateway;
    ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void prepareRequestCancellation(Long requestId) {
        Instant now = Instant.now();
        paymentRepository.findByRequestIdForUpdate(requestId).stream()
                .filter(payment -> payment.getStatus() == ConsultationPaymentStatus.PENDING
                        || payment.getStatus() == ConsultationPaymentStatus.EXPIRED
                        || payment.getStatus() == ConsultationPaymentStatus.FAILED)
                .forEach(payment -> {
                    payment.setStatus(ConsultationPaymentStatus.CANCELLED);
                    payment.setCancelledAt(now);
                    payment.setProviderCancellationStatus(PaymentProviderCancellationStatus.PENDING);
                    payment.setProviderCancellationRequestedAt(now);
                    payment.setProviderCancellationError(null);
                    paymentRepository.save(payment);
                });
    }

    @Override
    public void cancelProviderLinksAfterCommit(Long requestId) {
        Runnable action = () -> eventPublisher.publishEvent(
                new PaymentProviderCancellationRequestedEvent(requestId));
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }

    @Override
    @Transactional
    public void reconcileProviderCancellation(Long actorId, UserRole role, Long paymentId) {
        if (role != UserRole.ADMIN && role != UserRole.SUPER_ADMIN)
            throw new AppException(ErrorCode.ACCESS_DENIED,
                    "Only Admin or Super Admin may reconcile provider cancellation");
        ConsultationPayment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_PAYMENT_NOT_FOUND));
        if (payment.getProviderCancellationStatus() != PaymentProviderCancellationStatus.FAILED
                && payment.getProviderCancellationStatus() != PaymentProviderCancellationStatus.PENDING)
            return;
        cancelOne(payment);
    }

    private void cancelOne(ConsultationPayment payment) {
        Instant now = Instant.now();
        payment.setProviderCancellationLastAttemptAt(now);
        try {
            paymentGateway.cancelPaymentLink(payment.getOrderCode(), "HealthSense care request cancelled locally");
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
