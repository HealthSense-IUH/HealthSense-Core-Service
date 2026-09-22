package fit.iuh.se.hsbilling.repository;

import fit.iuh.se.hsbilling.entity.CreditReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

public interface CreditReservationRepository extends JpaRepository<CreditReservation, Long> {
    Optional<CreditReservation> findByRequestId(Long requestId);
    Optional<CreditReservation> findBySessionId(Long sessionId);
    @Query("select coalesce(sum(r.quantity),0) from CreditReservation r where r.walletId=:walletId and r.status=fit.iuh.se.hsbilling.entity.enums.CreditReservationStatus.HELD")
    long sumHeldQuantity(Long walletId);
}
