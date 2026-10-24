package fit.iuh.se.hsassistant.entity;

/** Vòng đời ingest của một tài liệu tri thức. */
public enum KnowledgeDocumentStatus {
	/** Đã lưu file, đang chờ worker trên cloud nhận message. */
	PENDING,
	/** Worker đang tách đoạn + embedding. */
	PROCESSING,
	/** Đã có vector trong Qdrant, trợ lý tra cứu được. */
	READY,
	/** Ingest lỗi; xem error_message, có thể bấm "Ingest lại". */
	FAILED
}
