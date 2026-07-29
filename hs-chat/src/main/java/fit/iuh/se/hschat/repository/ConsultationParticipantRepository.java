package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.ConsultationParticipant;
import fit.iuh.se.hschat.entity.enums.ConsultationParticipantRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationParticipantRepository extends MongoRepository<ConsultationParticipant, String> {

    boolean existsBySessionIdAndUserIdAndActiveTrue(String sessionId, Long userId);

    Optional<ConsultationParticipant> findBySessionIdAndUserId(String sessionId, Long userId);

    Optional<ConsultationParticipant> findBySessionIdAndUserIdAndActiveTrue(String sessionId, Long userId);

    List<ConsultationParticipant> findBySessionIdAndActiveTrue(String sessionId);

    List<ConsultationParticipant> findByUserIdAndActiveTrue(Long userId);

    Optional<ConsultationParticipant> findBySessionIdAndRoleAndActiveTrue(
            String sessionId,
            ConsultationParticipantRole role
    );
}
