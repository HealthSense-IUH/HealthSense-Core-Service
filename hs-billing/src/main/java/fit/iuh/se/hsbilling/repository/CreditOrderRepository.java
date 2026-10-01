package fit.iuh.se.hsbilling.repository;

import fit.iuh.se.hsbilling.entity.CreditPurchaseOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CreditOrderRepository extends JpaRepository<CreditPurchaseOrder, Long>,
        JpaSpecificationExecutor<CreditPurchaseOrder>, CreditOrderAggregateRepository {
    Optional<CreditPurchaseOrder> findByMemberIdAndIdempotencyKey(Long memberId, String idempotencyKey);

    Optional<CreditPurchaseOrder> findByIdAndMemberId(Long id, Long memberId);

    Page<CreditPurchaseOrder> findByMemberId(Long memberId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from CreditPurchaseOrder o where o.id = :id")
    Optional<CreditPurchaseOrder> findByIdForUpdate(Long id);

    @Query("""
            select o.memberId as memberId,
                   coalesce(sum(o.amountVnd), 0) as totalPaidVnd,
                   coalesce(sum(o.creditQuantity), 0) as totalPurchasedCredits,
                   count(o.id) as successfulOrderCount
              from CreditPurchaseOrder o
             where o.status = fit.iuh.se.hsbilling.entity.enums.CreditOrderStatus.PAID
               and o.memberId in :memberIds
             group by o.memberId
            """)
    List<MemberCreditPaymentAggregate> summarizeSuccessfulPaymentsByMemberIds(
            @Param("memberIds") Collection<Long> memberIds);
}
