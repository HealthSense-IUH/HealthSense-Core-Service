package fit.iuh.se.hsbilling.repository;

import fit.iuh.se.hsbilling.entity.CreditPurchaseOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;
import java.time.Instant;
import fit.iuh.se.hsbilling.entity.enums.*;

public interface CreditOrderRepository extends JpaRepository<CreditPurchaseOrder, Long>, JpaSpecificationExecutor<CreditPurchaseOrder> {
    Optional<CreditPurchaseOrder> findByMemberIdAndIdempotencyKey(Long memberId, String idempotencyKey);
    Optional<CreditPurchaseOrder> findByIdAndMemberId(Long id, Long memberId);
    Page<CreditPurchaseOrder> findByMemberId(Long memberId, Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from CreditPurchaseOrder o where o.id = :id")
    Optional<CreditPurchaseOrder> findByIdForUpdate(Long id);
}
