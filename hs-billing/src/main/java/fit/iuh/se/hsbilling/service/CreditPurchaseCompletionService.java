package fit.iuh.se.hsbilling.service;

import fit.iuh.se.hsbilling.config.CreditPaymentConfiguration;
import fit.iuh.se.hsbilling.dto.*;
import fit.iuh.se.hsbilling.entity.enums.*;
import fit.iuh.se.hsbilling.event.CreditPurchaseCompleted;
import fit.iuh.se.hsbilling.repository.*;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.*;
import fit.iuh.se.hsoperations.dto.command.*;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.event.OperationalEventPublisher;
import java.util.List;
import java.util.Map;
import java.time.Instant;
import java.util.Objects;

/** Internal-only settlement boundary. Phase 5 adds verified PAYOS evidence here, not another wallet credit path. */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreditPurchaseCompletionService {
    private final CreditOrderRepository orders;
    private final CreditPaymentAttemptRepository attempts;
    private final ConsultationCreditService credits;
    private final CreditPaymentConfiguration configuration;
    private final ApplicationEventPublisher events;
    private final OperationalEventPublisher operationalEvents;

    @Transactional
    public CreditWalletResponse completePurchase(Long orderId, Long attemptId, VerifiedCreditPayment evidence) {
        if (orderId == null || attemptId == null || evidence == null)
            throw new AppException(ErrorCode.INVALID_CREDIT_PAYMENT);
        var order = orders.findByIdForUpdate(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.CREDIT_ORDER_NOT_FOUND));
        var attempt = attempts.findByIdAndOrderId(attemptId, orderId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDIT_PAYMENT));
        if (attempt.getProvider() == CreditPaymentProvider.MOCK) configuration.requireMockEnabled();
        boolean validReference = attempt.getProvider() == CreditPaymentProvider.MOCK
                ? Objects.equals(evidence.reference(), "mock:" + attemptId)
                : (attempt.getPaymentLinkId() == null || Objects.equals(evidence.paymentLinkId(), attempt.getPaymentLinkId()))
                    && evidence.reference() != null && !evidence.reference().isBlank();
        if (attempt.getProvider() != evidence.provider() || !validReference
                || evidence.amountVnd() != order.getAmountVnd()
                || !Objects.equals(evidence.currency(), order.getCurrency()))
            throw new AppException(ErrorCode.INVALID_CREDIT_PAYMENT);
        if (order.getStatus() == CreditOrderStatus.PAID) {
            if (attempt.getStatus() != CreditPaymentStatus.PAID
                    || !Objects.equals(attempt.getProviderReference(), evidence.reference())
                    || !Objects.equals(attempt.getVerifiedAmountVnd(), evidence.amountVnd())
                    || !Objects.equals(attempt.getVerifiedCurrency(), evidence.currency()))
                throw new AppException(ErrorCode.INVALID_CREDIT_PAYMENT);
            return credits.getWallet(order.getMemberId());
        }
        boolean payableAttempt = attempt.getStatus() == CreditPaymentStatus.PENDING
                || (attempt.getProvider() == CreditPaymentProvider.PAYOS
                    && attempt.getStatus() == CreditPaymentStatus.CREATING);
        if (order.getStatus() != CreditOrderStatus.PENDING_PAYMENT || !payableAttempt)
            throw new AppException(ErrorCode.INVALID_CREDIT_PAYMENT);

        // All state below joins the caller's transaction. No HTTP provider call is made while holding locks.
        var wallet = credits.credit(order.getMemberId(), order.getCreditQuantity(),
                new CreditSource(orderId), orderId.toString());
        Instant now = Instant.now();
        order.setStatus(CreditOrderStatus.PAID);
        order.setPaidAt(now);
        attempt.setStatus(CreditPaymentStatus.PAID);
        if (attempt.getPaymentLinkId() == null) attempt.setPaymentLinkId(evidence.paymentLinkId());
        attempt.setPaidAt(now);
        attempt.setProviderReference(evidence.reference());
        attempt.setVerifiedAmountVnd(evidence.amountVnd());
        attempt.setVerifiedCurrency(evidence.currency());
        attempts.saveAndFlush(attempt);
        orders.saveAndFlush(order);
        operationalEvents.record(OperationalEventCommand.builder()
                .domainType(BusinessDomainType.CONSULTATION_CREDIT).domainId(orderId)
                .eventType(BusinessEventType.CONSULTATION_CREDIT_PURCHASED).actorType(BusinessActorType.USER)
                .actorUserId(order.getMemberId()).actorRole("MEMBER").memberId(order.getMemberId())
                .previousState(CreditOrderStatus.PENDING_PAYMENT.name()).newState(CreditOrderStatus.PAID.name())
                .idempotencyKey("credit-purchase:audit:" + orderId)
                .metadata(Map.of("quantity", Long.toString(order.getCreditQuantity()), "provider", attempt.getProvider().name()))
                .notifications(List.of(new NotificationIntent(order.getMemberId(), NotificationType.CONSULTATION_CREDIT_PURCHASED,
                        "Consultation credits added", order.getCreditQuantity() + " consultation credits were added to your wallet.",
                        BusinessDomainType.CONSULTATION_CREDIT, orderId, "credit-purchase:notification:" + orderId)))
                .build());
        var event = new CreditPurchaseCompleted(orderId, order.getMemberId(), order.getCreditQuantity(), attempt.getProvider());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                try { events.publishEvent(event); }
                catch (RuntimeException ex) { log.error("Credit purchase committed but event delivery failed for order {}", orderId, ex); }
            }
        });
        return wallet;
    }
}
