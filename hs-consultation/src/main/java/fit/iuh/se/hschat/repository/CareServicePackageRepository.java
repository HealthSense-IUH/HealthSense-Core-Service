package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.CareServicePackage;
import fit.iuh.se.hschat.entity.enums.CareServicePackageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CareServicePackageRepository extends JpaRepository<CareServicePackage, Long> {

    Optional<CareServicePackage> findByIdAndStatus(Long id, CareServicePackageStatus status);

    Page<CareServicePackage> findByStatusOrderByCreatedAtDesc(CareServicePackageStatus status, Pageable pageable);
}
