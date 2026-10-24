package fit.iuh.se.hsassistant.service;

import fit.iuh.se.hsassistant.dto.AssistantConversationResponse;
import fit.iuh.se.hsassistant.dto.AssistantMessageResponse;
import fit.iuh.se.hsassistant.entity.AssistantConversation;
import fit.iuh.se.hsassistant.repository.AssistantConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Quản lý hội thoại của từng người dùng: tạo, liệt kê, xoá, đọc lại lịch sử từ bộ nhớ của Spring AI.
 * Mọi thao tác đều kiểm tra hội thoại có thuộc về userId không trước khi đụng tới bộ nhớ.
 */
@Service
@RequiredArgsConstructor
public class AssistantConversationService {

	private static final int TITLE_MAX = 80;

	private final AssistantConversationRepository repository;
	private final ChatMemory chatMemory;

	@Transactional
	public AssistantConversationResponse create(Long userId) {
		Instant now = Instant.now();
		AssistantConversation c = AssistantConversation.builder()
				.id(UUID.randomUUID().toString())
				.userId(userId)
				.createdAt(now)
				.updatedAt(now)
				.build();
		return toResponse(repository.save(c));
	}

	@Transactional(readOnly = true)
	public List<AssistantConversationResponse> listMine(Long userId) {
		return repository.findTop20ByUserIdOrderByUpdatedAtDesc(userId).stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public AssistantConversation requireOwned(String conversationId, Long userId) {
		return repository.findByIdAndUserId(conversationId, userId)
				.orElseThrow(() -> new EntityNotFoundException("Không tìm thấy hội thoại " + conversationId));
	}

	/** Lịch sử tin nhắn người dùng và trợ lý (bỏ system và tool) để FE hiện lại khi mở hội thoại cũ. */
	@Transactional(readOnly = true)
	public List<AssistantMessageResponse> messages(String conversationId, Long userId) {
		requireOwned(conversationId, userId);
		return chatMemory.get(conversationId).stream()
				.filter(m -> m.getMessageType() == MessageType.USER || m.getMessageType() == MessageType.ASSISTANT)
				.filter(m -> m.getText() != null && !m.getText().isBlank())
				.map(this::toMessage)
				.toList();
	}

	/** Sau mỗi lượt: cập nhật thời điểm và đặt tiêu đề bằng câu hỏi đầu tiên nếu chưa có. */
	@Transactional
	public void touch(String conversationId, Long userId, String firstUserMessage) {
		AssistantConversation c = requireOwned(conversationId, userId);
		if (c.getTitle() == null && firstUserMessage != null && !firstUserMessage.isBlank()) {
			String t = firstUserMessage.strip().replaceAll("\\s+", " ");
			c.setTitle(t.length() > TITLE_MAX ? t.substring(0, TITLE_MAX - 1) + "…" : t);
		}
		c.setUpdatedAt(Instant.now());
		repository.save(c);
	}

	@Transactional
	public void delete(String conversationId, Long userId) {
		AssistantConversation c = requireOwned(conversationId, userId);
		chatMemory.clear(conversationId);
		repository.delete(c);
	}

	private AssistantConversationResponse toResponse(AssistantConversation c) {
		return new AssistantConversationResponse(c.getId(), c.getTitle(), c.getCreatedAt(), c.getUpdatedAt());
	}

	private AssistantMessageResponse toMessage(Message m) {
		String role = m.getMessageType() == MessageType.USER ? "user" : "assistant";
		return new AssistantMessageResponse(role, m.getText());
	}
}
