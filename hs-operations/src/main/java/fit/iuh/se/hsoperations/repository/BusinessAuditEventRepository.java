package fit.iuh.se.hsoperations.repository;

import fit.iuh.se.hsoperations.entity.BusinessAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface BusinessAuditEventRepository extends JpaRepository<BusinessAuditEvent, Long>,
        JpaSpecificationExecutor<BusinessAuditEvent> {
    Optional<BusinessAuditEvent> findByIdempotencyKey(String idempotencyKey);
}
