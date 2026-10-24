package fit.iuh.se.hsassistant.dto;

/** Một tin nhắn trong lịch sử hội thoại: role là "user" hoặc "assistant", content là Markdown. */
public record AssistantMessageResponse(String role, String content) {
}
