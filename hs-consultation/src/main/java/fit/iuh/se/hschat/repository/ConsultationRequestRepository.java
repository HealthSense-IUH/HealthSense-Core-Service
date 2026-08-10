package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.ConsultationRequest;
import fit.iuh.se.hschat.entity.enums.ConsultationRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Repository
public interface ConsultationRequestRepository extends JpaRepository<ConsultationRequest, Long>, JpaSpecificationExecutor<ConsultationRequest> {

    Page<ConsultationRequest> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    Page<ConsultationRequest> findByStatusOrderByCreatedAtDesc(
            ConsultationRequestStatus status,
            Pageable pageable
    );

    Page<ConsultationRequest> findByMemberIdAndStatusOrderByCreatedAtDesc(
            Long memberId,
            ConsultationRequestStatus status,
            Pageable pageable
    );

    Optional<ConsultationRequest> findByConsultationSessionId(Long consultationSessionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select request from ConsultationRequest request where request.id = :id")
    Optional<ConsultationRequest> findByIdForUpdate(Long id);

    boolean existsByMemberIdAndStatus(Long memberId, ConsultationRequestStatus status);

    boolean existsByMemberIdAndStatusIn(Long memberId, Collection<ConsultationRequestStatus> statuses);

    List<ConsultationRequest> findByStatusAndPaymentDeadlineBefore(
            ConsultationRequestStatus status,
            Instant paymentDeadline
    );

    long countByAssignedDoctorIdAndStatusAndPaymentDeadlineAfter(
            Long assignedDoctorId,
            ConsultationRequestStatus status,
            Instant paymentDeadline
    );
}
