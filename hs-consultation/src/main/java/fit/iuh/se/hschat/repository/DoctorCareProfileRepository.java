package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.DoctorCareProfile;
import fit.iuh.se.hschat.entity.enums.DoctorSpecialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorCareProfileRepository extends JpaRepository<DoctorCareProfile, Long> {

    Optional<DoctorCareProfile> findByDoctorId(Long doctorId);

    List<DoctorCareProfile> findByDoctorIdIn(Collection<Long> doctorIds);

    List<DoctorCareProfile> findBySpecialtyAndAcceptsOneOnOneCareTrue(DoctorSpecialty specialty);
}
