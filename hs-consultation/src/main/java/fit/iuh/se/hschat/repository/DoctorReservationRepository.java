package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.DoctorReservation;
import fit.iuh.se.hschat.entity.enums.DoctorReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DoctorReservationRepository extends JpaRepository<DoctorReservation, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select reservation from DoctorReservation reservation
            where reservation.requestId = :requestId and reservation.status = :status
            """)
    Optional<DoctorReservation> findByRequestIdAndStatusForUpdate(
            @Param("requestId") Long requestId,
            @Param("status") DoctorReservationStatus status
    );

    Optional<DoctorReservation> findByRequestIdAndStatus(Long requestId, DoctorReservationStatus status);

    long countByDoctorIdAndStatusAndExpiresAtAfter(
            Long doctorId,
            DoctorReservationStatus status,
            Instant expiresAt
    );

    long countByDoctorIdAndStatusAndExpiresAtAfterAndIdNot(
            Long doctorId,
            DoctorReservationStatus status,
            Instant expiresAt,
            Long excludedId
    );

    List<DoctorReservation> findByStatusAndExpiresAtBefore(
            DoctorReservationStatus status,
            Instant expiresAt
    );
}
