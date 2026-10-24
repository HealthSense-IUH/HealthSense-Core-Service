package fit.iuh.se.hsassistant.knowledge;

import fit.iuh.se.hsassistant.dto.KnowledgeIngestMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Consumer queue ingest. Chỉ tồn tại khi app.rag.ingest.enabled=true (instance cloud, nối được Ollama + Qdrant);
 * dev để false nên message dev gửi lên sẽ do cloud xử lý. Mọi lỗi đều được nuốt (đã ghi trạng thái FAILED)
 * để không requeue vô hạn.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rag.ingest.enabled", havingValue = "true")
public class KnowledgeIngestListener {

	private final KnowledgeIngestService ingestService;

	@RabbitListener(queues = "${app.rabbitmq.queue.ingest:document.ingest.queue}")
	public void onMessage(KnowledgeIngestMessage message) {
		if (message == null || message.getDocumentId() == null) {
			log.warn("Bỏ qua message ingest không hợp lệ: {}", message);
			return;
		}
		try {
			if (message.getAction() == KnowledgeIngestMessage.Action.DELETE) {
				ingestService.deleteVectors(message.getDocumentId());
			} else {
				ingestService.ingest(message.getDocumentId());
			}
		} catch (Exception e) {
			log.error("Xử lý message ingest {} lỗi: {}", message, e.toString(), e);
		}
	}
}
