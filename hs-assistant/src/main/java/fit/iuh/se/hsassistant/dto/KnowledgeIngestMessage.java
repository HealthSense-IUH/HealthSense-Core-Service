package fit.iuh.se.hsassistant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Message trên RabbitMQ (exchange chung của hệ thống, routing key document.ingest.routing).
 * Core ở bất kỳ môi trường nào cũng có thể gửi; chỉ instance bật app.rag.ingest.enabled (cloud, có Ollama) mới nhận.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeIngestMessage implements Serializable {

	public enum Action {
		/** Đọc file từ MinIO, tách đoạn, embedding, ghi Qdrant. */
		INGEST,
		/** Xoá toàn bộ vector của tài liệu (tài liệu đã bị xoá ở DB). */
		DELETE
	}

	String documentId;
	Action action;
}
