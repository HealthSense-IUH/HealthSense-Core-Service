package fit.iuh.se.hsassistant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

/**
 * Một hội thoại với trợ lý. Nội dung từng tin nhắn nằm ở bảng SPRING_AI_CHAT_MEMORY do Spring AI quản lý;
 * bảng này chỉ ghi hội thoại thuộc về ai để chặn người khác đọc trộm theo id.
 */
@Entity
@Table(name = "assistant_conversations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssistantConversation {

	@Id
	@Column(length = 36, nullable = false)
	String id;

	@Column(name = "user_id", nullable = false)
	Long userId;

	@Column(length = 160)
	String title;

	@Column(name = "created_at", nullable = false)
	Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	Instant updatedAt;
}
