package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.ConsultationPayment;
import fit.iuh.se.hschat.entity.enums.ConsultationPaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationPaymentRepository extends JpaRepository<ConsultationPayment, Long> {

    Optional<ConsultationPayment> findByRequestId(Long requestId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select payment from ConsultationPayment payment where payment.requestId = :requestId")
    Optional<ConsultationPayment> findByRequestIdForUpdate(Long requestId);

    Optional<ConsultationPayment> findByOrderCode(Long orderCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select payment from ConsultationPayment payment where payment.orderCode = :orderCode")
    Optional<ConsultationPayment> findByOrderCodeForUpdate(Long orderCode);

    boolean existsByOrderCode(Long orderCode);

    List<ConsultationPayment> findByStatusInAndExpiresAtBefore(Collection<ConsultationPaymentStatus> statuses, Instant expiresAt);
}
