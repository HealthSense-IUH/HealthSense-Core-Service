package fit.iuh.se.hsbilling.service;

import fit.iuh.se.hsbilling.dto.*;
import fit.iuh.se.hsbilling.entity.enums.*;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsuser.entity.enums.AccountStatus;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.springframework.data.domain.Pageable;
import java.time.Instant;
import java.util.List;

public interface CreditAdministrationService {
    AdminCreditPackageResponse createPackage(Long actorId, UserRole role, AdminCreditPackageRequest request);
    AdminCreditPackageResponse updatePackage(Long actorId, UserRole role, Long id, AdminCreditPackageRequest request);
    List<AdminCreditPackageResponse> getPackages(UserRole role);
    PageResponse<AdminMemberCreditSummary> getMembers(Long actorId, UserRole role, AccountStatus status,
            String keyword, Pageable pageable);
    CreditWalletResponse getWallet(UserRole role, Long memberId);
    PageResponse<AdminCreditLedgerResponse> getLedger(UserRole role, Long memberId, CreditOperation operation,
            CreditSourceType sourceType, Instant from, Instant to, Pageable pageable);
    PageResponse<AdminCreditOrderSummary> getOrders(UserRole role, Long memberId, CreditOrderStatus status,
            CreditPaymentProvider provider, Instant from, Instant to, Pageable pageable);
    AdminCreditOrderResponse getOrder(UserRole role, Long orderId);
    CreditMutationResponse adjust(Long actorId, UserRole role, Long memberId, long delta, String reason, String key);
    CreditMutationResponse refundCapturedSession(Long actorId, UserRole role, Long memberId, Long sessionId,
            long quantity, String reason, String key);
    CreditWalletReconciliationResponse reconcile(UserRole role, Long memberId);
}
