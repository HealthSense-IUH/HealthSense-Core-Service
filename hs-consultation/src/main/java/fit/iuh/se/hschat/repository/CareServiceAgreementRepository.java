package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.CareServiceAgreement;
import fit.iuh.se.hschat.entity.enums.CareServiceAgreementStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CareServiceAgreementRepository extends JpaRepository<CareServiceAgreement, Long> {

    Optional<CareServiceAgreement> findFirstByRequestIdOrderByCreatedAtDesc(Long requestId);

    List<CareServiceAgreement> findByRequestIdOrderByCreatedAtAsc(Long requestId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CareServiceAgreement> findFirstByRequestIdAndStatusInOrderByCreatedAtDesc(
            Long requestId,
            Collection<CareServiceAgreementStatus> statuses
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CareServiceAgreement> findByIdAndMemberId(Long id, Long memberId);

    Optional<CareServiceAgreement> findFirstByRenewalIdOrderByCreatedAtDesc(Long renewalId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CareServiceAgreement> findFirstByRenewalIdAndStatusInOrderByCreatedAtDesc(
            Long renewalId,
            Collection<CareServiceAgreementStatus> statuses);
}
