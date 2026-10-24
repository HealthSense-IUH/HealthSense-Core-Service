package fit.iuh.se.hsassistant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Một tài liệu trong kho tri thức của trợ lý. File gốc nằm ở MinIO/S3 (objectKey), vector nằm ở Qdrant
 * (metadata documentId = id này); bảng chỉ giữ thông tin hiển thị và trạng thái ingest.
 */
@Entity
@Table(name = "knowledge_documents")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KnowledgeDocument {

	@Id
	@Column(length = 36, nullable = false)
	String id;

	@Column(nullable = false, length = 200)
	String title;

	@Column(name = "file_name", nullable = false)
	String fileName;

	@Column(name = "content_type", length = 120)
	String contentType;

	@Column(name = "size_bytes", nullable = false)
	long sizeBytes;

	@Column(name = "object_key", nullable = false, length = 512)
	String objectKey;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	KnowledgeDocumentStatus status;

	@Column(name = "chunk_count", nullable = false)
	int chunkCount;

	@Column(name = "error_message", length = 1000)
	String errorMessage;

	@Column(name = "uploaded_by")
	Long uploadedBy;

	@Column(name = "created_at", nullable = false)
	Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	Instant updatedAt;

	@Column(name = "processed_at")
	Instant processedAt;
}
