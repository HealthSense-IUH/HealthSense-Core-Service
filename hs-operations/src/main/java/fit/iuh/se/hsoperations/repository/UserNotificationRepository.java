package fit.iuh.se.hsoperations.repository;

import fit.iuh.se.hsoperations.entity.UserNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    Page<UserNotification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);
    Optional<UserNotification> findByIdAndRecipientId(Long id, Long recipientId);
    long countByRecipientIdAndReadAtIsNull(Long recipientId);
    boolean existsByIdempotencyKey(String idempotencyKey);

    @Modifying
    @Query("update UserNotification n set n.readAt = :readAt where n.recipientId = :recipientId and n.readAt is null")
    int markAllRead(Long recipientId, java.time.Instant readAt);
}
