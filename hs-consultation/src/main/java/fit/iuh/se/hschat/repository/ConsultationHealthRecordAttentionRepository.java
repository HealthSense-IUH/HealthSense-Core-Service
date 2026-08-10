package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.ConsultationHealthRecordAttention;
import fit.iuh.se.hschat.entity.enums.ConsultationAttentionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationHealthRecordAttentionRepository extends JpaRepository<ConsultationHealthRecordAttention, Long> {

    boolean existsBySessionIdAndHealthRecordId(Long sessionId, Long healthRecordId);

    Optional<ConsultationHealthRecordAttention> findBySessionIdAndHealthRecordId(Long sessionId, Long healthRecordId);

    List<ConsultationHealthRecordAttention> findBySessionIdInAndStatus(Collection<Long> sessionIds, ConsultationAttentionStatus status);

    long countBySessionIdAndStatus(Long sessionId, ConsultationAttentionStatus status);
}
