package fit.iuh.se.hsbilling.service.impl;

import fit.iuh.se.hsbilling.config.CreditPaymentConfiguration;
import fit.iuh.se.hsbilling.dto.*;
import fit.iuh.se.hsbilling.entity.*;
import fit.iuh.se.hsbilling.entity.enums.*;
import fit.iuh.se.hsbilling.payment.MockCreditPaymentGateway;
import fit.iuh.se.hsbilling.payment.CreditPaymentGateway;
import fit.iuh.se.hsbilling.repository.*;
import fit.iuh.se.hsbilling.service.*;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.*;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CreditPurchaseServiceImpl implements CreditPurchaseService {
    private final CreditOrderRepository orders;
    private final CreditPaymentAttemptRepository attempts;
    private final CreditPackageRepository packages;
    private final UserAccountRepository users;
    private final ConsultationCreditService credits;
    private final CreditPurchaseCompletionService completion;
    private final MockCreditPaymentGateway gateway;
    private final CreditPaymentConfiguration configuration;
    private final CreditPaymentGateway payOSGateway;
    private final PaymentOrderCodeAllocator orderCodes;
    private final TransactionTemplate transactions;

    @Override
    public CreditOrderResponse createOrder(Long memberId, Long packageId, String idempotencyKey) {
        positiveId(memberId);
        positiveId(packageId);
        if (idempotencyKey == null || !idempotencyKey.matches("[A-Za-z0-9._:-]{1,128}"))
            throw new AppException(ErrorCode.INVALID_PARAMETER, "Idempotency-Key must contain 1-128 ASCII letters, digits, '.', '_', ':' or '-'");
        var provider = configuration.requireEnabledProvider();
        Start start = transactions.execute(status -> start(memberId, packageId, idempotencyKey, provider));
        if (start == null) throw new AppException(ErrorCode.UNCATEGORIZED);
        if (!start.created()) return getOrder(memberId, start.orderId());
        if (provider == CreditPaymentProvider.MOCK) {
            completion.completePurchase(start.orderId(), start.attemptId(),
                    gateway.pay(start.attemptId(), start.amountVnd(), "VND"));
        } else {
            createPayOSLink(start);
        }
        return getOrder(memberId, start.orderId());
    }

    private Start start(Long memberId, Long packageId, String key, CreditPaymentProvider provider) {
        requireMember(users.findByIdForUpdate(memberId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND)));
        String fingerprint = "credit-order:v1:package:" + packageId;
        var existing = orders.findByMemberIdAndIdempotencyKey(memberId, key).orElse(null);
        if (existing != null) {
            if (!existing.getRequestFingerprint().equals(fingerprint))
                throw new AppException(ErrorCode.CREDIT_IDEMPOTENCY_CONFLICT);
            var attempt = attempts.findFirstByOrderIdOrderByAttemptNumberDesc(existing.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.DATA_INTEGRITY_VIOLATION));
            return new Start(existing.getId(), attempt.getId(), existing.getAmountVnd(), false,
                    attempt.getOrderCode(), attempt.getExpiresAt());
        }
        var pack = packages.findById(packageId).filter(p -> p.getStatus() == CreditPackageStatus.ACTIVE)
                .orElseThrow(() -> new AppException(ErrorCode.CREDIT_PACKAGE_UNAVAILABLE));
        Instant now = Instant.now();
        var order = CreditPurchaseOrder.builder().memberId(memberId).packageId(packageId)
                .packageCode(pack.getCode()).packageName(pack.getName()).creditQuantity(pack.getCreditQuantity())
                .amountVnd(pack.getPriceVnd()).currency("VND").status(CreditOrderStatus.PENDING_PAYMENT)
                .idempotencyKey(key).requestFingerprint(fingerprint).build();
        order.setCreatedAt(now); orders.saveAndFlush(order);
        Long orderCode = provider == CreditPaymentProvider.PAYOS ? orderCodes.next() : null;
        Instant expiresAt = provider == CreditPaymentProvider.PAYOS ? now.plus(configuration.linkTtl()) : null;
        var attempt = CreditPaymentAttempt.builder().orderId(order.getId()).attemptNumber(1).provider(provider)
                .status(provider == CreditPaymentProvider.PAYOS ? CreditPaymentStatus.CREATING : CreditPaymentStatus.PENDING)
                .orderCode(orderCode).expiresAt(expiresAt).build();
        attempt.setCreatedAt(now); attempts.saveAndFlush(attempt);
        return new Start(order.getId(), attempt.getId(), order.getAmountVnd(), true, orderCode, expiresAt);
    }

    private void createPayOSLink(Start start) {
        try {
            var link = payOSGateway.createPaymentLink(start.orderCode(), start.amountVnd(),
                    "HS CREDIT", configuration.returnUrl(), configuration.cancelUrl(), start.expiresAt());
            transactions.executeWithoutResult(status -> {
                var attempt = attempts.findByIdAndOrderId(start.attemptId(), start.orderId())
                        .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDIT_PAYMENT));
                if (attempt.getStatus() == CreditPaymentStatus.CREATING) {
                    attempt.setPaymentLinkId(link.paymentLinkId());
                    attempt.setCheckoutUrl(link.checkoutUrl());
                    attempt.setStatus(CreditPaymentStatus.PENDING);
                    attempts.saveAndFlush(attempt);
                }
            });
        } catch (AppException ex) { throw ex; }
        catch (RuntimeException ex) {
            transactions.executeWithoutResult(status -> attempts.findById(start.attemptId()).ifPresent(a -> {
                a.setLastError("PayOS link creation outcome is unknown"); attempts.save(a);
            }));
            throw new AppException(ErrorCode.PAYMENT_PROVIDER_ERROR, "Unable to create PayOS checkout link");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CreditOrderResponse getOrder(Long memberId, Long orderId) {
        member(memberId);
        positiveId(orderId);
        return response(orders.findByIdAndMemberId(orderId, memberId)
                .orElseThrow(() -> new AppException(ErrorCode.CREDIT_ORDER_NOT_FOUND)));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CreditOrderSummary> getOrders(Long memberId, Pageable pageable) {
        member(memberId);
        if (pageable == null || pageable.isUnpaged() || pageable.getPageSize() > 100)
            throw new AppException(ErrorCode.INVALID_PARAMETER);
        var ordered = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        return new PageResponse<>(orders.findByMemberId(memberId, ordered).map(CreditOrderSummary::from));
    }

    @Override
    public CreditOrderResponse cancelOrder(Long memberId, Long orderId) {
        member(memberId); positiveId(orderId);
        var order = orders.findByIdAndMemberId(orderId, memberId)
                .orElseThrow(() -> new AppException(ErrorCode.CREDIT_ORDER_NOT_FOUND));
        if (order.getStatus() != CreditOrderStatus.PENDING_PAYMENT) return response(order);
        var attempt = attempts.findFirstByOrderIdOrderByAttemptNumberDesc(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.DATA_INTEGRITY_VIOLATION));
        if (attempt.getProvider() != CreditPaymentProvider.PAYOS || attempt.getOrderCode() == null)
            throw new AppException(ErrorCode.INVALID_CREDIT_PAYMENT);
        try { payOSGateway.cancelPaymentLink(attempt.getOrderCode(), "Member requested cancellation"); }
        catch (RuntimeException ex) { throw new AppException(ErrorCode.PAYMENT_PROVIDER_ERROR, "Unable to cancel PayOS checkout"); }
        transactions.executeWithoutResult(status -> {
            var lockedOrder = orders.findByIdForUpdate(orderId)
                    .orElseThrow(() -> new AppException(ErrorCode.CREDIT_ORDER_NOT_FOUND));
            var lockedAttempt = attempts.findByIdForUpdate(attempt.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDIT_PAYMENT));
            if (lockedOrder.getStatus() == CreditOrderStatus.PENDING_PAYMENT
                    && lockedAttempt.getStatus() != CreditPaymentStatus.PAID) {
                lockedOrder.setStatus(CreditOrderStatus.CANCELLED);
                lockedAttempt.setStatus(CreditPaymentStatus.CANCELLED);
                lockedAttempt.setCancelledAt(Instant.now());
                orders.saveAndFlush(lockedOrder); attempts.saveAndFlush(lockedAttempt);
            }
        });
        return getOrder(memberId, orderId);
    }

    private CreditOrderResponse response(CreditPurchaseOrder order) {
        var attempt = attempts.findFirstByOrderIdOrderByAttemptNumberDesc(order.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DATA_INTEGRITY_VIOLATION));
        return new CreditOrderResponse(CreditOrderSummary.from(order),
                new CreditPaymentSummary(attempt.getId().toString(), attempt.getProvider(), attempt.getStatus(),
                        attempt.getOrderCode() == null ? null : attempt.getOrderCode().toString(),
                        attempt.getPaymentLinkId(), attempt.getCheckoutUrl(), attempt.getExpiresAt()),
                credits.getWallet(order.getMemberId()));
    }

    private void member(Long memberId) {
        if (memberId == null) throw new AppException(ErrorCode.UNAUTHORIZED);
        requireMember(users.findById(memberId).orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND)));
    }

    private void requireMember(UserAccount account) {
        if (account.getRole() != UserRole.MEMBER) throw new AppException(ErrorCode.ACCESS_DENIED);
        if (account.getStatus() != AccountStatus.ACTIVE) throw new AppException(ErrorCode.ACCOUNT_DISABLED);
    }

    private void positiveId(Long id) {
        if (id == null || id <= 0) throw new AppException(ErrorCode.INVALID_PARAMETER);
    }

    private record Start(Long orderId, Long attemptId, long amountVnd, boolean created,
            Long orderCode, Instant expiresAt) {}
}
