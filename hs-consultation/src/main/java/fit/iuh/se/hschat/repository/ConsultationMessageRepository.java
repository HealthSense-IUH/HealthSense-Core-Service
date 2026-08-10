package fit.iuh.se.hschat.repository;

import fit.iuh.se.hschat.entity.ConsultationMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationMessageRepository extends MongoRepository<ConsultationMessage, String> {

    List<ConsultationMessage> findBySessionIdOrderByCreatedAtDesc(Long sessionId, Pageable pageable);

    List<ConsultationMessage> findBySessionIdAndActiveTrueOrderByCreatedAtDesc(Long sessionId, Pageable pageable);

    List<ConsultationMessage> findBySessionIdAndCreatedAtBeforeOrderByCreatedAtDesc(
            Long sessionId,
            Instant beforeCreatedAt,
            Pageable pageable
    );

    List<ConsultationMessage> findBySessionIdAndActiveTrueAndCreatedAtBeforeOrderByCreatedAtDesc(
            Long sessionId,
            Instant beforeCreatedAt,
            Pageable pageable
    );

    Optional<ConsultationMessage> findBySessionIdAndSenderIdAndClientMessageId(
            Long sessionId,
            Long senderId,
            String clientMessageId
    );

    long countBySessionIdAndCreatedAtAfterAndSenderIdNot(
            Long sessionId,
            Instant lastReadAt,
            Long senderId
    );

    long countBySessionIdAndSenderIdNot(Long sessionId, Long senderId);
}
