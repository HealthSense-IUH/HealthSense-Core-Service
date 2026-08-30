package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.ConsultationRefund;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ConsultationRefundRepository extends JpaRepository<ConsultationRefund, Long> {

    Optional<ConsultationRefund> findByPaymentId(Long paymentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select refund from ConsultationRefund refund where refund.id = :id")
    Optional<ConsultationRefund> findByIdForUpdate(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select refund from ConsultationRefund refund where refund.paymentId = :paymentId")
    Optional<ConsultationRefund> findByPaymentIdForUpdate(Long paymentId);
}
