package fit.iuh.se.hsbilling.service.impl;

import cn.hutool.core.lang.Snowflake;
import fit.iuh.se.hsbilling.dto.*;
import fit.iuh.se.hsbilling.entity.*;
import fit.iuh.se.hsbilling.entity.enums.*;
import fit.iuh.se.hsbilling.repository.*;
import fit.iuh.se.hsbilling.service.CreditAdministrationService;
import fit.iuh.se.hsoperations.dto.command.*;
import fit.iuh.se.hsoperations.entity.enums.*;
import fit.iuh.se.hsoperations.event.OperationalEventPublisher;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.UserAccount;
import fit.iuh.se.hsuser.entity.enums.*;
import fit.iuh.se.hsuser.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreditAdministrationServiceImpl implements CreditAdministrationService {
    private final CreditPackageRepository packages;
    private final CreditWalletRepository wallets;
    private final CreditLedgerRepository ledger;
    private final CreditReservationRepository reservations;
    private final CreditOrderRepository orders;
    private final CreditPaymentAttemptRepository attempts;
    private final UserAccountRepository users;
    private final Snowflake ids;
    private final OperationalEventPublisher events;

    @Override @Transactional
    public AdminCreditPackageResponse createPackage(Long actorId, UserRole role, AdminCreditPackageRequest r) {
        requireAdmin(actorId, role); validateCreate(r);
        Instant now = Instant.now();
        var pack = CreditPackage.builder().code(r.code().trim()).name(r.name().trim())
                .description(trim(r.description())).creditQuantity(r.creditQuantity()).priceVnd(r.priceVnd())
                .status(CreditPackageStatus.INACTIVE).build();
        pack.setCreatedAt(now);
        try { packages.saveAndFlush(pack); }
        catch (DataIntegrityViolationException ex) { throw new AppException(ErrorCode.DATA_INTEGRITY_VIOLATION, "Credit package code already exists"); }
        audit(pack.getId(), BusinessEventType.CREDIT_PACKAGE_CREATED, actorId, role, null,
                pack.getStatus().name(), "Credit package created", Map.of("code", pack.getCode(), "version", pack.getVersion().toString()));
        return AdminCreditPackageResponse.from(pack);
    }

    @Override @Transactional
    public AdminCreditPackageResponse updatePackage(Long actorId, UserRole role, Long id, AdminCreditPackageRequest r) {
        requireAdmin(actorId, role); positiveId(id);
        if (r == null || r.version() == null) invalid("version is required");
        var pack = packages.findById(id).orElseThrow(() -> new AppException(ErrorCode.CREDIT_PACKAGE_NOT_FOUND));
        if (!Objects.equals(pack.getVersion(), r.version())) throw new AppException(ErrorCode.CREDIT_PACKAGE_VERSION_CONFLICT);
        String previous = pack.getStatus().name();
        if (r.name() != null) { if (r.name().isBlank() || r.name().length() > 160) invalid("name must contain 1-160 characters"); pack.setName(r.name().trim()); }
        if (r.description() != null) { if (r.description().length() > 1000) invalid("description is too long"); pack.setDescription(trim(r.description())); }
        if (r.creditQuantity() != null) { positive(r.creditQuantity(), "creditQuantity"); pack.setCreditQuantity(r.creditQuantity()); }
        if (r.priceVnd() != null) { positive(r.priceVnd(), "priceVnd"); pack.setPriceVnd(r.priceVnd()); }
        if (r.status() != null) pack.setStatus(r.status());
        try { packages.saveAndFlush(pack); }
        catch (OptimisticLockingFailureException ex) { throw new AppException(ErrorCode.CREDIT_PACKAGE_VERSION_CONFLICT); }
        audit(pack.getId(), BusinessEventType.CREDIT_PACKAGE_UPDATED, actorId, role, previous,
                pack.getStatus().name(), "Credit package updated", Map.of("code", pack.getCode(), "version", pack.getVersion().toString()));
        return AdminCreditPackageResponse.from(pack);
    }

    @Override public List<AdminCreditPackageResponse> getPackages(UserRole role) {
        requireAdminRole(role);
        return packages.findAll(Sort.by("creditQuantity").ascending().and(Sort.by("id"))).stream()
                .map(AdminCreditPackageResponse::from).toList();
    }

    @Override public PageResponse<AdminMemberCreditSummary> getMembers(Long actorId, UserRole role,
            AccountStatus status, String keyword, Pageable pageable) {
        requireAdmin(actorId, role); page(pageable);
        String normalizedKeyword = trim(keyword);
        Page<UserAccount> members = normalizedKeyword == null
                ? users.findUsers(UserRole.MEMBER, status, actorId, pageable)
                : users.searchUsers(UserRole.MEMBER, status, actorId, normalizedKeyword, pageable);
        List<Long> memberIds = members.getContent().stream().map(UserAccount::getId).toList();
        Map<Long, CreditWallet> walletsByMember = memberIds.isEmpty() ? Map.of()
                : wallets.findAllByMemberIdIn(memberIds).stream()
                        .collect(Collectors.toMap(CreditWallet::getMemberId, wallet -> wallet));
        return new PageResponse<>(members.map(member -> memberSummary(member, walletsByMember.get(member.getId()))));
    }

    @Override public CreditWalletResponse getWallet(UserRole role, Long memberId) {
        requireAdminRole(role); requireMember(memberId);
        return wallets.findByMemberId(memberId).map(this::walletResponse).orElse(new CreditWalletResponse(0,0,0));
    }

    @Override public PageResponse<AdminCreditLedgerResponse> getLedger(UserRole role, Long memberId,
            CreditOperation operation, CreditSourceType sourceType, Instant from, Instant to, Pageable pageable) {
        requireAdminRole(role); requireMember(memberId); page(pageable); range(from, to);
        Pageable ordered = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        return wallets.findByMemberId(memberId)
                .map(w -> new PageResponse<>(ledger.findAll(ledgerSpec(w.getId(), operation, sourceType, from, to), ordered)
                        .map(AdminCreditLedgerResponse::from)))
                .orElseGet(() -> new PageResponse<>(Page.empty(ordered)));
    }

    @Override public PageResponse<AdminCreditOrderSummary> getOrders(UserRole role, Long memberId, CreditOrderStatus status,
            CreditPaymentProvider provider, Instant from, Instant to, Pageable pageable) {
        requireAdminRole(role); if (memberId != null) requireMember(memberId); page(pageable); range(from,to);
        Pageable ordered = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        return new PageResponse<>(orders.findAll(orderSpec(memberId, status, provider, from, to), ordered)
                .map(AdminCreditOrderSummary::from));
    }

    @Override public AdminCreditOrderResponse getOrder(UserRole role, Long orderId) {
        requireAdminRole(role); positiveId(orderId);
        var order = orders.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.CREDIT_ORDER_NOT_FOUND));
        var history = attempts.findByOrderIdOrderByAttemptNumberAsc(orderId).stream().map(a ->
                new CreditPaymentSummary(a.getId().toString(), a.getProvider(), a.getStatus(),
                        a.getOrderCode() == null ? null : a.getOrderCode().toString(),
                        a.getPaymentLinkId(), a.getCheckoutUrl(), a.getExpiresAt())).toList();
        return new AdminCreditOrderResponse(order.getMemberId().toString(), CreditOrderSummary.from(order), history);
    }

    @Override @Transactional
    public CreditMutationResponse adjust(Long actorId, UserRole role, Long memberId, long delta, String reason, String key) {
        requireAdmin(actorId, role); requireMember(memberId); mutation(reason, key);
        if (delta == 0 || delta == Long.MIN_VALUE) invalid("delta must be non-zero");
        CreditWallet wallet = lockWallet(memberId);
        String fullKey = "admin:adjust:" + actorId + ":" + key;
        var prior = ledger.findByIdempotencyKey(fullKey).orElse(null);
        if (prior != null) {
            if (!prior.getWalletId().equals(wallet.getId()) || prior.getDeltaBalance() != delta
                    || !Objects.equals(prior.getActorId(), actorId) || !Objects.equals(prior.getReason(), reason.trim())) conflict();
            return mutationResponse(prior, wallet);
        }
        if (delta < 0 && wallet.getBalance() - wallet.getReserved() < -delta)
            throw new AppException(ErrorCode.INSUFFICIENT_CONSULTATION_CREDITS,
                    "Negative adjustment exceeds available credits");
        try { wallet.setBalance(Math.addExact(wallet.getBalance(), delta)); }
        catch (ArithmeticException ex) { throw new AppException(ErrorCode.CREDIT_BALANCE_OVERFLOW); }
        long actionId = ids.nextId();
        var entry = append(null, wallet, CreditOperation.ADJUSTMENT, Math.abs(delta), delta, 0,
                CreditSourceType.ADMIN_ADJUSTMENT, actionId, null, fullKey, actorId, reason.trim());
        audit(entry.getId(), BusinessEventType.CREDIT_WALLET_ADJUSTED, actorId, role, null,
                Long.toString(delta), reason.trim(), Map.of("memberId", memberId.toString()));
        return mutationResponse(entry, wallet);
    }

    @Override @Transactional
    public CreditMutationResponse refundCapturedSession(Long actorId, UserRole role, Long memberId, Long sessionId,
            long quantity, String reason, String key) {
        requireAdmin(actorId, role); requireMember(memberId); positiveId(sessionId); positive(quantity, "quantity"); mutation(reason,key);
        CreditWallet wallet = lockExistingWallet(memberId);
        var debit = ledger.findByOperationAndSourceTypeAndSourceId(CreditOperation.CAPTURE,
                CreditSourceType.CONSULTATION_SESSION, sessionId).orElse(null);
        if (debit != null) {
            var reservation = reservations.findBySessionId(sessionId)
                    .filter(r -> r.getWalletId().equals(wallet.getId())
                            && r.getStatus() == CreditReservationStatus.CAPTURED
                            && r.getQuantity() == quantity)
                    .orElseThrow(() -> new AppException(ErrorCode.CREDIT_REFUND_NOT_ELIGIBLE));
            if (!debit.getWalletId().equals(reservation.getWalletId()) || debit.getQuantity() != quantity)
                throw new AppException(ErrorCode.DATA_INTEGRITY_VIOLATION);
        } else {
            debit = ledger.findByOperationAndSourceTypeAndSourceId(CreditOperation.SESSION_CHARGE,
                    CreditSourceType.CONSULTATION_SESSION, sessionId)
                    .filter(entry -> entry.getWalletId().equals(wallet.getId()) && entry.getQuantity() == quantity)
                    .orElseThrow(() -> new AppException(ErrorCode.CREDIT_REFUND_NOT_ELIGIBLE));
        }
        var prior = ledger.findByOperationAndSourceTypeAndSourceId(CreditOperation.SESSION_REFUND,
                CreditSourceType.CONSULTATION_SESSION, sessionId).orElse(null);
        if (prior != null) return mutationResponse(prior, wallet);
        try { wallet.setBalance(Math.addExact(wallet.getBalance(), quantity)); }
        catch (ArithmeticException ex) { throw new AppException(ErrorCode.CREDIT_BALANCE_OVERFLOW); }
        var entry = append(null, wallet, CreditOperation.SESSION_REFUND, quantity, quantity, 0,
                CreditSourceType.CONSULTATION_SESSION, sessionId, debit.getId(), "admin:session-refund:" + actorId + ":" + key,
                actorId, reason.trim());
        events.record(OperationalEventCommand.builder().domainType(BusinessDomainType.CONSULTATION_CREDIT)
                .domainId(entry.getId()).eventType(BusinessEventType.CONSULTATION_CREDIT_REFUNDED)
                .actorType(BusinessActorType.USER).actorUserId(actorId).actorRole(role.name()).sessionId(sessionId)
                .memberId(memberId).reason(reason.trim()).idempotencyKey("credit-refund:audit:" + sessionId)
                .metadata(Map.of("quantity", Long.toString(quantity), "chargedEntryId", debit.getId().toString()))
                .notifications(List.of(new NotificationIntent(memberId, NotificationType.CONSULTATION_CREDIT_REFUNDED,
                        "Consultation credit refunded", quantity + " consultation credit has been returned to your wallet.",
                        BusinessDomainType.CONSULTATION_CREDIT, entry.getId(), "credit-refund:notification:" + sessionId)))
                .build());
        return mutationResponse(entry, wallet);
    }

    @Override @Transactional(isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public CreditWalletReconciliationResponse reconcile(UserRole role, Long memberId) {
        requireAdminRole(role); requireMember(memberId);
        var wallet = wallets.findByMemberId(memberId).orElse(null);
        if (wallet == null) return new CreditWalletReconciliationResponse(memberId.toString(),0,0,0,0,0,true);
        long lb = ledger.balanceTotal(wallet.getId()), lr = ledger.reservedTotal(wallet.getId());
        long held = reservations.sumHeldQuantity(wallet.getId());
        return new CreditWalletReconciliationResponse(memberId.toString(), wallet.getBalance(), wallet.getReserved(), lb, lr,
                held, wallet.getBalance()==lb && wallet.getReserved()==lr && wallet.getReserved()==held);
    }

    private CreditWallet lockWallet(Long memberId) {
        wallets.initialize(ids.nextId(), memberId);
        return lockExistingWallet(memberId);
    }
    private Specification<CreditLedgerEntry> ledgerSpec(Long walletId, CreditOperation operation,
            CreditSourceType sourceType, Instant from, Instant to) {
        Specification<CreditLedgerEntry> spec=(root,query,cb)->cb.equal(root.get("walletId"),walletId);
        if(operation!=null) spec=spec.and((root,query,cb)->cb.equal(root.get("operation"),operation));
        if(sourceType!=null) spec=spec.and((root,query,cb)->cb.equal(root.get("sourceType"),sourceType));
        if(from!=null) spec=spec.and((root,query,cb)->cb.greaterThanOrEqualTo(root.get("createdAt"),from));
        if(to!=null) spec=spec.and((root,query,cb)->cb.lessThan(root.get("createdAt"),to));
        return spec;
    }
    private Specification<CreditPurchaseOrder> orderSpec(Long memberId, CreditOrderStatus status,
            CreditPaymentProvider provider, Instant from, Instant to) {
        Specification<CreditPurchaseOrder> spec=(root,query,cb)->cb.conjunction();
        if(memberId!=null) spec=spec.and((root,query,cb)->cb.equal(root.get("memberId"),memberId));
        if(status!=null) spec=spec.and((root,query,cb)->cb.equal(root.get("status"),status));
        if(from!=null) spec=spec.and((root,query,cb)->cb.greaterThanOrEqualTo(root.get("createdAt"),from));
        if(to!=null) spec=spec.and((root,query,cb)->cb.lessThan(root.get("createdAt"),to));
        if(provider!=null) spec=spec.and((root,query,cb)->{
            var sub=query.subquery(Long.class); var attempt=sub.from(CreditPaymentAttempt.class);
            sub.select(attempt.get("id")).where(cb.equal(attempt.get("orderId"),root.get("id")),
                    cb.equal(attempt.get("provider"),provider)); return cb.exists(sub);
        });
        return spec;
    }
    private CreditWallet lockExistingWallet(Long memberId) {
        return wallets.findByMemberIdForUpdate(memberId).orElseThrow(() -> new AppException(ErrorCode.CREDIT_RESERVATION_NOT_FOUND));
    }
    private CreditLedgerEntry append(Long id, CreditWallet wallet, CreditOperation operation, long quantity,
            long deltaBalance, long deltaReserved, CreditSourceType sourceType, Long sourceId, Long related,
            String key, Long actorId, String reason) {
        wallets.saveAndFlush(wallet);
        var e = CreditLedgerEntry.builder().id(id).walletId(wallet.getId()).operation(operation).quantity(quantity)
                .deltaBalance(deltaBalance).deltaReserved(deltaReserved).balanceAfter(wallet.getBalance())
                .reservedAfter(wallet.getReserved()).sourceType(sourceType).sourceId(sourceId).relatedEntryId(related)
                .idempotencyKey(key).actorId(actorId).reason(reason).build();
        e.setCreatedAt(Instant.now()); return ledger.save(e);
    }
    private CreditMutationResponse mutationResponse(CreditLedgerEntry e, CreditWallet w) {
        return new CreditMutationResponse(AdminCreditLedgerResponse.from(e), walletResponse(w));
    }
    private AdminMemberCreditSummary memberSummary(UserAccount member, CreditWallet wallet) {
        var profile = member.getProfile();
        long balance = wallet == null ? 0 : wallet.getBalance();
        long reserved = wallet == null ? 0 : wallet.getReserved();
        return new AdminMemberCreditSummary(member.getId().toString(), profile.getDisplayName(), member.getEmail(),
                profile.getPhone(), member.getStatus(), profile.getAvatarUrl(), wallet != null, balance, reserved,
                balance - reserved, wallet == null ? null : wallet.getUpdatedAt());
    }
    private CreditWalletResponse walletResponse(CreditWallet w) { return new CreditWalletResponse(w.getBalance(),w.getReserved(),w.getBalance()-w.getReserved()); }
    private void audit(Long id, BusinessEventType type, Long actor, UserRole role, String previous, String next,
            String reason, Map<String,String> metadata) {
        events.record(OperationalEventCommand.builder().domainType(BusinessDomainType.CONSULTATION_CREDIT).domainId(id)
                .eventType(type).actorType(BusinessActorType.USER).actorUserId(actor).actorRole(role.name())
                .previousState(previous).newState(next).reason(reason).metadata(metadata)
                .idempotencyKey("credit-admin:audit:" + type + ":" + id + ":"
                        + metadata.getOrDefault("version", next == null ? "none" : next)).build());
    }
    private void requireAdmin(Long actor, UserRole role) { positiveId(actor); requireAdminRole(role); }
    private void requireAdminRole(UserRole role) { if (role != UserRole.ADMIN && role != UserRole.SUPER_ADMIN) throw new AppException(ErrorCode.ACCESS_DENIED); }
    private void requireMember(Long id) { positiveId(id); var u=users.findById(id).orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND)); if(u.getRole()!=UserRole.MEMBER) throw new AppException(ErrorCode.MEMBER_NOT_FOUND); }
    private void validateCreate(AdminCreditPackageRequest r) { if(r==null || r.code()==null || !r.code().matches("[A-Z0-9_-]{2,80}")) invalid("code must contain 2-80 uppercase letters, digits, '_' or '-'"); if(r.name()==null||r.name().isBlank()||r.name().length()>160) invalid("name must contain 1-160 characters"); if(r.description()!=null&&r.description().length()>1000) invalid("description is too long"); positive(r.creditQuantity(),"creditQuantity"); positive(r.priceVnd(),"priceVnd"); }
    private void mutation(String reason,String key) { if(reason==null||reason.isBlank()||reason.length()>500) invalid("reason must contain 1-500 characters"); if(key==null||!key.matches("[A-Za-z0-9._:-]{1,128}")) invalid("A valid Idempotency-Key is required"); }
    private String trim(String s) { return s==null||s.isBlank()?null:s.trim(); }
    private void positive(Long n,String name) { if(n==null||n<=0) invalid(name+" must be positive"); }
    private void positive(long n,String name) { if(n<=0) invalid(name+" must be positive"); }
    private void positiveId(Long n) { if(n==null||n<=0) invalid("id must be positive"); }
    private void page(Pageable p) { if(p==null||p.isUnpaged()||p.getPageSize()<1||p.getPageSize()>100) invalid("page size must be between 1 and 100"); }
    private void range(Instant from,Instant to) { if(from!=null&&to!=null&&!from.isBefore(to)) invalid("from must be before to"); }
    private void invalid(String m) { throw new AppException(ErrorCode.INVALID_PARAMETER,m); }
    private void conflict() { throw new AppException(ErrorCode.CREDIT_IDEMPOTENCY_CONFLICT); }
}
