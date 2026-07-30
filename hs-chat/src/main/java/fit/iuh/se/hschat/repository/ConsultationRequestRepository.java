package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.ConsultationRequest;
import fit.iuh.se.hschat.entity.enums.ConsultationRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsultationRequestRepository extends JpaRepository<ConsultationRequest, Long> {

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

    boolean existsByMemberIdAndStatus(Long memberId, ConsultationRequestStatus status);
}
