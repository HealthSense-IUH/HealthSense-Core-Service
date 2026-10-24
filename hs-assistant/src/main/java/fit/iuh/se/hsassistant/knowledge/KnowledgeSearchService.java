package fit.iuh.se.hsassistant.knowledge;

import fit.iuh.se.hsassistant.dto.KnowledgeHit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Tra cứu kho tri thức bằng tìm kiếm ngữ nghĩa trên Qdrant. Chỉ bật khi app.rag.enabled=true
 * (cần tới được Qdrant và Ollama để embedding câu hỏi).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rag.enabled", havingValue = "true", matchIfMissing = true)
public class KnowledgeSearchService {

	static final double MIN_SIMILARITY = 0.3;
	static final int SNIPPET_MAX_CHARS = 1200;
	/** Embedding câu hỏi + truy vấn Qdrant quá lâu thì bỏ qua, để trợ lý vẫn trả lời được. */
	static final long SEARCH_TIMEOUT_SECONDS = 12;

	private final VectorStore vectorStore;

	public List<KnowledgeHit> search(String query, int topK) {
		SearchRequest request = SearchRequest.builder()
				.query(query)
				.topK(topK)
				.similarityThreshold(MIN_SIMILARITY)
				.build();
		CompletableFuture<List<Document>> future = CompletableFuture.supplyAsync(() -> vectorStore.similaritySearch(request));
		try {
			return future.get(SEARCH_TIMEOUT_SECONDS, TimeUnit.SECONDS).stream().map(KnowledgeSearchService::toHit).toList();
		} catch (TimeoutException e) {
			future.cancel(true);
			log.warn("Tra cứu kho tri thức quá {} giây, bỏ qua", SEARCH_TIMEOUT_SECONDS);
			return List.of();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return List.of();
		} catch (Exception e) {
			log.warn("Tra cứu kho tri thức lỗi: {}", e.getCause() != null ? e.getCause().toString() : e.toString());
			return List.of();
		}
	}

	static KnowledgeHit toHit(Document d) {
		Map<String, Object> m = d.getMetadata();
		String text = d.getText() == null ? "" : d.getText();
		return new KnowledgeHit(
				str(m.get("documentId")),
				str(m.get("title")),
				str(m.get("fileName")),
				KnowledgeIngestService.abbreviate(text, SNIPPET_MAX_CHARS),
				d.getScore());
	}

	private static String str(Object o) {
		return o == null ? null : o.toString();
	}
}
