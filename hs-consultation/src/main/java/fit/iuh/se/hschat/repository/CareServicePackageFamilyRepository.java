package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.CareServicePackageFamily;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CareServicePackageFamilyRepository extends JpaRepository<CareServicePackageFamily, Long> {

    boolean existsByCode(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select family from CareServicePackageFamily family where family.id = :familyId")
    Optional<CareServicePackageFamily> findByIdForUpdate(@Param("familyId") Long familyId);
}
