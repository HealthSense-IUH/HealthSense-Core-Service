package fit.iuh.se.hsassistant.dto;

import fit.iuh.se.hsassistant.entity.KnowledgeDocument;
import fit.iuh.se.hsassistant.entity.KnowledgeDocumentStatus;

import java.time.Instant;

/** Tài liệu tri thức trả về cho trang quản trị. */
public record KnowledgeDocumentResponse(
		String id,
		String title,
		String fileName,
		String contentType,
		long sizeBytes,
		KnowledgeDocumentStatus status,
		int chunkCount,
		String errorMessage,
		Long uploadedBy,
		Instant createdAt,
		Instant updatedAt,
		Instant processedAt
) {
	public static KnowledgeDocumentResponse from(KnowledgeDocument d) {
		return new KnowledgeDocumentResponse(d.getId(), d.getTitle(), d.getFileName(), d.getContentType(),
				d.getSizeBytes(), d.getStatus(), d.getChunkCount(), d.getErrorMessage(), d.getUploadedBy(),
				d.getCreatedAt(), d.getUpdatedAt(), d.getProcessedAt());
	}
}
