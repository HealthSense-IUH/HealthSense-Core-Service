package fit.iuh.se.hshealthrecord.repository;

import fit.iuh.se.hshealthrecord.entity.HealthRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long>, JpaSpecificationExecutor<HealthRecord> {

    Page<HealthRecord> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<HealthRecord> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<HealthRecord> findByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);
}
