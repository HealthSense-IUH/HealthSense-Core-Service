package fit.iuh.se.hsbilling.repository;

import fit.iuh.se.hsbilling.entity.CreditPackage;
import fit.iuh.se.hsbilling.entity.enums.CreditPackageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CreditPackageRepository extends JpaRepository<CreditPackage, Long> {
    List<CreditPackage> findByStatusOrderByCreditQuantityAscIdAsc(CreditPackageStatus status);
}
