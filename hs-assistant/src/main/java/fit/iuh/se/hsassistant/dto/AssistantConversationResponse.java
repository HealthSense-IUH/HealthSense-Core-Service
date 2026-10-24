package fit.iuh.se.hsassistant.dto;

import java.time.Instant;

public record AssistantConversationResponse(String id, String title, Instant createdAt, Instant updatedAt) {
}
