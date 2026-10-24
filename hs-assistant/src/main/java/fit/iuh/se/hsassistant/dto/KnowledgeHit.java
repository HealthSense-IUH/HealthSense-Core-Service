package fit.iuh.se.hsassistant.dto;

/** Một đoạn tài liệu liên quan tới câu hỏi, trả cho tool searchKnowledge của trợ lý. */
public record KnowledgeHit(String documentId, String title, String fileName, String snippet, Double score) {
}
