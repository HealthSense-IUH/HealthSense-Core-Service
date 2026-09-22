package fit.iuh.se.hsbilling.repository;

import fit.iuh.se.hsbilling.entity.CreditPaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import fit.iuh.se.hsbilling.entity.enums.*;
import java.time.Instant;
import java.util.Collection;

public interface CreditPaymentAttemptRepository extends JpaRepository<CreditPaymentAttempt, Long> {
    Optional<CreditPaymentAttempt> findByIdAndOrderId(Long id, Long orderId);
    Optional<CreditPaymentAttempt> findFirstByOrderIdOrderByAttemptNumberDesc(Long orderId);
    List<CreditPaymentAttempt> findByOrderIdOrderByAttemptNumberAsc(Long orderId);
    Optional<CreditPaymentAttempt> findByOrderCode(Long orderCode);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from CreditPaymentAttempt a where a.id = :id")
    Optional<CreditPaymentAttempt> findByIdForUpdate(Long id);
    List<CreditPaymentAttempt> findTop100ByProviderAndStatusInAndExpiresAtBeforeOrderByExpiresAtAsc(
            CreditPaymentProvider provider, Collection<CreditPaymentStatus> statuses, Instant expiresAt);
}
