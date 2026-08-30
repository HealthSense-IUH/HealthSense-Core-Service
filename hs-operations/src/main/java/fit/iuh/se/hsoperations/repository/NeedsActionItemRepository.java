package fit.iuh.se.hsoperations.repository;

import fit.iuh.se.hsoperations.entity.NeedsActionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface NeedsActionItemRepository extends JpaRepository<NeedsActionItem, Long>,
        JpaSpecificationExecutor<NeedsActionItem> {
    Optional<NeedsActionItem> findByIdempotencyKey(String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<NeedsActionItem> findLockedById(Long id);
}
