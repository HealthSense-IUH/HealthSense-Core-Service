package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationSessionRepository extends MongoRepository<ConsultationSession, String> {

    Optional<ConsultationSession> findByRequestId(String requestId);

    Optional<ConsultationSession> findByMemberIdAndStatus(Long memberId, ConsultationStatus status);

    boolean existsByMemberIdAndStatus(Long memberId, ConsultationStatus status);

    long countByDoctorIdAndStatus(Long doctorId, ConsultationStatus status);

    Page<ConsultationSession> findByMemberIdOrderByLastMessageAtDesc(Long memberId, Pageable pageable);

    Page<ConsultationSession> findByDoctorIdOrderByLastMessageAtDesc(Long doctorId, Pageable pageable);

    Page<ConsultationSession> findByStatusOrderByCreatedAtDesc(ConsultationStatus status, Pageable pageable);

    List<ConsultationSession> findByStatusAndEndsAtBefore(ConsultationStatus status, Instant endsAt);
}
