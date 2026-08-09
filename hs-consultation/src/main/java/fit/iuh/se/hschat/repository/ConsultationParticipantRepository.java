package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.ConsultationParticipant;
import fit.iuh.se.hschat.entity.enums.ConsultationParticipantRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationParticipantRepository extends JpaRepository<ConsultationParticipant, Long> {

    boolean existsBySessionIdAndUserIdAndActiveTrue(Long sessionId, Long userId);

    Optional<ConsultationParticipant> findBySessionIdAndUserId(Long sessionId, Long userId);

    Optional<ConsultationParticipant> findBySessionIdAndUserIdAndActiveTrue(Long sessionId, Long userId);

    List<ConsultationParticipant> findBySessionIdAndActiveTrue(Long sessionId);

    List<ConsultationParticipant> findByUserIdAndActiveTrue(Long userId);

    Optional<ConsultationParticipant> findBySessionIdAndRoleAndActiveTrue(
            Long sessionId,
            ConsultationParticipantRole role
    );
}
