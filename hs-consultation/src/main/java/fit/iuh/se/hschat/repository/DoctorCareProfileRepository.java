package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.DoctorCareProfile;
import fit.iuh.se.hschat.entity.enums.DoctorSpecialty;
import fit.iuh.se.hschat.entity.enums.DoctorDispatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorCareProfileRepository extends JpaRepository<DoctorCareProfile, Long> {

    Optional<DoctorCareProfile> findByDoctorId(Long doctorId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select profile from DoctorCareProfile profile where profile.doctorId = :doctorId")
    Optional<DoctorCareProfile> findByDoctorIdForUpdate(Long doctorId);

    List<DoctorCareProfile> findByDoctorIdIn(Collection<Long> doctorIds);

    List<DoctorCareProfile> findBySpecialtyAndAcceptsOneOnOneCareTrue(DoctorSpecialty specialty);

    long countByDispatchStatus(DoctorDispatchStatus dispatchStatus);

    List<DoctorCareProfile> findByDispatchStatusOrderByDoctorIdAsc(DoctorDispatchStatus dispatchStatus);
}
