package fit.iuh.se.hsbilling.service.impl;

import cn.hutool.core.lang.Snowflake;
import fit.iuh.se.hsbilling.dto.*;
import fit.iuh.se.hsbilling.entity.*;
import fit.iuh.se.hsbilling.entity.enums.*;
import fit.iuh.se.hsbilling.repository.*;
import fit.iuh.se.hsbilling.service.ConsultationCreditService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.enums.*;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConsultationCreditServiceImpl implements ConsultationCreditService {
    private final CreditWalletRepository wallets;
    private final CreditLedgerRepository ledger;
    private final CreditReservationRepository reservations;
    private final CreditPackageRepository packages;
    private final UserAccountRepository users;
    private final Snowflake ids;

    @Override
    public List<CreditPackageResponse> getPackages(Long memberId) {
        requireMember(memberId, true);
        return packages.findByStatusOrderByCreditQuantityAscIdAsc(CreditPackageStatus.ACTIVE).stream()
                .map(p -> new CreditPackageResponse(p.getId().toString(), p.getCode(), p.getName(), p.getDescription(),
                        p.getCreditQuantity(), p.getPriceVnd())).toList();
    }

    @Override
    public CreditWalletResponse getWallet(Long memberId) {
        requireMember(memberId, true);
        return wallets.findByMemberId(memberId).map(this::walletResponse)
                .orElse(new CreditWalletResponse(0, 0, 0));
    }

    @Override
    public PageResponse<CreditLedgerResponse> getLedger(Long memberId, Pageable pageable) {
        requireMember(memberId, true);
        if (pageable == null || pageable.isUnpaged() || pageable.getPageSize() > 100)
            throw new AppException(ErrorCode.INVALID_PARAMETER, "Page size must be between 1 and 100");
        // Do not allow callers to reorder financial history inconsistently between pages.
        Pageable ordered = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        return wallets.findByMemberId(memberId)
                .map(w -> new PageResponse<>(ledger.findByWalletId(w.getId(), ordered).map(this::ledgerResponse)))
                .orElseGet(() -> new PageResponse<>(Page.empty(ordered)));
    }

    @Override
    @Transactional
    public CreditWalletResponse credit(Long memberId, long quantity, CreditSource source, String idempotencyKey) {
        positive(quantity);
        if (source == null) throw new AppException(ErrorCode.INVALID_PARAMETER);
        positiveId(source.purchaseOrderId());
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 128)
            throw new AppException(ErrorCode.INVALID_PARAMETER, "Idempotency key must contain 1 to 128 characters");
        requireMember(memberId, true);
        CreditWallet wallet = lockWallet(memberId);
        String key = "credit:purchase:" + idempotencyKey;
        CreditLedgerEntry existing = ledger.findByIdempotencyKey(key).orElse(null);
        if (existing != null) {
            if (!existing.getWalletId().equals(wallet.getId()) || existing.getQuantity() != quantity
                    || existing.getOperation() != CreditOperation.PURCHASE
                    || existing.getSourceType() != CreditSourceType.PURCHASE_ORDER
                    || !existing.getSourceId().equals(source.purchaseOrderId())) conflict();
            return walletResponse(wallet);
        }
        // A different key must not fund the same purchase order twice.
        if (ledger.findByOperationAndSourceTypeAndSourceId(CreditOperation.PURCHASE,
                CreditSourceType.PURCHASE_ORDER, source.purchaseOrderId()).isPresent()) conflict();
        try {
            wallet.setBalance(Math.addExact(wallet.getBalance(), quantity));
        } catch (ArithmeticException ex) {
            throw new AppException(ErrorCode.CREDIT_BALANCE_OVERFLOW);
        }
        append(wallet, CreditOperation.PURCHASE, quantity, quantity, 0,
                CreditSourceType.PURCHASE_ORDER, source.purchaseOrderId(), key, null);
        return walletResponse(wallet);
    }

    @Override
    @Transactional
    public void requireAvailable(Long memberId, long quantity) {
        positive(quantity);
        requireMember(memberId, true);
        CreditWallet wallet = wallets.findByMemberIdForUpdate(memberId).orElse(null);
        long available = wallet == null ? 0 : wallet.getBalance() - wallet.getReserved();
        if (available < quantity) insufficient(quantity, available);
    }

    @Override
    @Transactional
    public CreditWalletResponse chargeSession(Long memberId, Long requestId, Long sessionId, long quantity) {
        positiveId(requestId);
        positiveId(sessionId);
        positive(quantity);
        requireMember(memberId, true);
        CreditWallet wallet = lockExistingWallet(memberId);
        String key = "credit:session-charge:" + requestId;
        CreditLedgerEntry existing = ledger.findByIdempotencyKey(key).orElse(null);
        if (existing != null) {
            if (!existing.getWalletId().equals(wallet.getId()) || existing.getQuantity() != quantity
                    || existing.getOperation() != CreditOperation.SESSION_CHARGE
                    || existing.getSourceType() != CreditSourceType.CONSULTATION_SESSION
                    || !existing.getSourceId().equals(sessionId)) conflict();
            return walletResponse(wallet);
        }
        if (ledger.findByOperationAndSourceTypeAndSourceId(CreditOperation.SESSION_CHARGE,
                CreditSourceType.CONSULTATION_SESSION, sessionId).isPresent()) conflict();
        long available = wallet.getBalance() - wallet.getReserved();
        if (available < quantity) insufficient(quantity, available);
        wallet.setBalance(Math.subtractExact(wallet.getBalance(), quantity));
        append(wallet, CreditOperation.SESSION_CHARGE, quantity, -quantity, 0,
                CreditSourceType.CONSULTATION_SESSION, sessionId, key, null);
        return walletResponse(wallet);
    }

    @Override
    @Transactional
    public CreditReservationResponse reserve(Long memberId, Long requestId, long quantity) {
        positiveId(requestId);
        positive(quantity);
        requireMember(memberId, true);
        CreditWallet wallet = lockWallet(memberId);
        CreditReservation existing = reservations.findByRequestId(requestId).orElse(null);
        if (existing != null) {
            requireOwner(existing, wallet);
            if (existing.getQuantity() != quantity) conflict();
            // Retrying admission after capture/release returns the existing state; never reopens it.
            return reservationResponse(existing, wallet);
        }
        long available = wallet.getBalance() - wallet.getReserved();
        if (available < quantity) insufficient(quantity, available);
        wallet.setReserved(Math.addExact(wallet.getReserved(), quantity));
        CreditReservation reservation = CreditReservation.builder().walletId(wallet.getId())
                .requestId(requestId).quantity(quantity).status(CreditReservationStatus.HELD)
                .heldAt(Instant.now()).build();
        reservation.setCreatedAt(reservation.getHeldAt());
        reservations.save(reservation);
        append(wallet, CreditOperation.RESERVE, quantity, 0, quantity,
                CreditSourceType.CONSULTATION_REQUEST, requestId, "credit:reserve:" + requestId, null);
        return reservationResponse(reservation, wallet);
    }

    @Override
    @Transactional
    public CreditReservationResponse capture(Long memberId, Long requestId, Long sessionId) {
        positiveId(requestId);
        positiveId(sessionId);
        requireMember(memberId, true);
        CreditWallet wallet = lockExistingWallet(memberId);
        CreditReservation reservation = reservation(requestId, wallet);
        if (reservation.getStatus() == CreditReservationStatus.CAPTURED) {
            if (!Objects.equals(reservation.getSessionId(), sessionId)) conflict();
            return reservationResponse(reservation, wallet);
        }
        requireHeld(reservation);
        if (reservations.findBySessionId(sessionId).isPresent()) conflict();
        long quantity = reservation.getQuantity();
        wallet.setBalance(Math.subtractExact(wallet.getBalance(), quantity));
        wallet.setReserved(Math.subtractExact(wallet.getReserved(), quantity));
        reservation.setStatus(CreditReservationStatus.CAPTURED);
        reservation.setSessionId(sessionId);
        reservation.setCapturedAt(Instant.now());
        reservations.save(reservation);
        append(wallet, CreditOperation.CAPTURE, quantity, -quantity, -quantity,
                CreditSourceType.CONSULTATION_SESSION, sessionId, "credit:capture:" + requestId, null);
        return reservationResponse(reservation, wallet);
    }

    @Override
    @Transactional
    public CreditReservationResponse release(Long memberId, Long requestId, String reason) {
        positiveId(requestId);
        if (reason == null || reason.isBlank() || reason.length() > 500)
            throw new AppException(ErrorCode.INVALID_PARAMETER, "A release reason of up to 500 characters is required");
        // System timeout/cancellation must still release a hold after account deactivation.
        requireMember(memberId, false);
        CreditWallet wallet = lockExistingWallet(memberId);
        CreditReservation reservation = reservation(requestId, wallet);
        if (reservation.getStatus() == CreditReservationStatus.RELEASED) {
            var prior = ledger.findByIdempotencyKey("credit:release:" + requestId)
                    .orElseThrow(() -> new AppException(ErrorCode.DATA_INTEGRITY_VIOLATION));
            if (!Objects.equals(prior.getReason(), reason)) conflict();
            return reservationResponse(reservation, wallet);
        }
        requireHeld(reservation);
        long quantity = reservation.getQuantity();
        wallet.setReserved(Math.subtractExact(wallet.getReserved(), quantity));
        reservation.setStatus(CreditReservationStatus.RELEASED);
        reservation.setReleasedAt(Instant.now());
        reservations.save(reservation);
        append(wallet, CreditOperation.RELEASE, quantity, 0, -quantity,
                CreditSourceType.CONSULTATION_REQUEST, requestId, "credit:release:" + requestId, reason);
        return reservationResponse(reservation, wallet);
    }

    @Override
    public CreditReservationResponse inspectReservation(Long memberId, Long requestId) {
        positiveId(requestId);
        requireMember(memberId, false);
        CreditWallet wallet = wallets.findByMemberId(memberId)
                .orElseThrow(() -> new AppException(ErrorCode.CREDIT_RESERVATION_NOT_FOUND));
        return reservationResponse(reservation(requestId, wallet), wallet);
    }

    private void requireMember(Long memberId, boolean requireActive) {
        if (memberId == null) throw new AppException(ErrorCode.UNAUTHORIZED);
        var member = users.findById(memberId).orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
        if (member.getRole() != UserRole.MEMBER) throw new AppException(ErrorCode.ACCESS_DENIED);
        if (requireActive && member.getStatus() != AccountStatus.ACTIVE)
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
    }

    private CreditWallet lockWallet(Long memberId) {
        // PostgreSQL serializes concurrent first writes without poisoning a transaction on duplicate insert.
        wallets.initialize(ids.nextId(), memberId);
        return wallets.findByMemberIdForUpdate(memberId)
                .orElseThrow(() -> new AppException(ErrorCode.DATA_INTEGRITY_VIOLATION));
    }

    private CreditWallet lockExistingWallet(Long memberId) {
        return wallets.findByMemberIdForUpdate(memberId)
                .orElseThrow(() -> new AppException(ErrorCode.CREDIT_RESERVATION_NOT_FOUND));
    }

    private CreditReservation reservation(Long requestId, CreditWallet wallet) {
        var reservation = reservations.findByRequestId(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.CREDIT_RESERVATION_NOT_FOUND));
        requireOwner(reservation, wallet);
        return reservation;
    }

    private void requireOwner(CreditReservation reservation, CreditWallet wallet) {
        if (!reservation.getWalletId().equals(wallet.getId())) conflict();
    }

    private void requireHeld(CreditReservation reservation) {
        if (reservation.getStatus() != CreditReservationStatus.HELD)
            throw new AppException(ErrorCode.INVALID_CREDIT_RESERVATION_STATUS);
    }

    private void append(CreditWallet wallet, CreditOperation operation, long quantity,
            long deltaBalance, long deltaReserved, CreditSourceType sourceType, Long sourceId,
            String key, String reason) {
        wallets.saveAndFlush(wallet);
        var entry = CreditLedgerEntry.builder().walletId(wallet.getId()).operation(operation).quantity(quantity)
                .deltaBalance(deltaBalance).deltaReserved(deltaReserved).balanceAfter(wallet.getBalance())
                .reservedAfter(wallet.getReserved()).sourceType(sourceType).sourceId(sourceId)
                .idempotencyKey(key).reason(reason).build();
        entry.setCreatedAt(Instant.now());
        ledger.save(entry);
    }

    private CreditWalletResponse walletResponse(CreditWallet wallet) {
        return new CreditWalletResponse(wallet.getBalance(), wallet.getReserved(), wallet.getBalance() - wallet.getReserved());
    }

    private CreditReservationResponse reservationResponse(CreditReservation reservation, CreditWallet wallet) {
        return new CreditReservationResponse(reservation.getRequestId(), reservation.getSessionId(),
                reservation.getQuantity(), reservation.getStatus(), walletResponse(wallet));
    }

    private CreditLedgerResponse ledgerResponse(CreditLedgerEntry entry) {
        return new CreditLedgerResponse(entry.getId().toString(), entry.getOperation(), entry.getQuantity(),
                entry.getDeltaBalance(), entry.getDeltaReserved(), entry.getBalanceAfter(), entry.getReservedAfter(),
                entry.getSourceType(), entry.getSourceId().toString(), entry.getCreatedAt());
    }

    private void positiveId(Long value) {
        if (value == null || value <= 0) throw new AppException(ErrorCode.INVALID_PARAMETER);
    }

    private void positive(long value) {
        if (value <= 0) throw new AppException(ErrorCode.INVALID_PARAMETER, "Credit quantity must be positive");
    }

    private void insufficient(long required, long available) {
        throw new AppException(ErrorCode.INSUFFICIENT_CONSULTATION_CREDITS,
                "Insufficient consultation credits: requiredCredits=" + required
                        + ", availableCredits=" + available);
    }

    private void conflict() { throw new AppException(ErrorCode.CREDIT_IDEMPOTENCY_CONFLICT); }
}
