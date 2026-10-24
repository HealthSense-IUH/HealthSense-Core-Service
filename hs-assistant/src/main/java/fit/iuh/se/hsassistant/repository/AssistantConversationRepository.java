package fit.iuh.se.hsassistant.repository;

import fit.iuh.se.hsassistant.entity.AssistantConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssistantConversationRepository extends JpaRepository<AssistantConversation, String> {

	List<AssistantConversation> findTop20ByUserIdOrderByUpdatedAtDesc(Long userId);

	Optional<AssistantConversation> findByIdAndUserId(String id, Long userId);
}
