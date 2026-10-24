package fit.iuh.se.hsassistant.knowledge;

import fit.iuh.se.hsassistant.entity.KnowledgeDocument;
import fit.iuh.se.hsassistant.entity.KnowledgeDocumentStatus;
import fit.iuh.se.hsassistant.repository.KnowledgeDocumentRepository;
import fit.iuh.se.hsshared.service.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Pipeline ingest chạy ở instance có Ollama (cloud): tải file từ MinIO, Tika đọc text (PDF/DOCX/...),
 * tách đoạn theo token, embedding rồi ghi Qdrant kèm metadata documentId/title để lọc và trích nguồn.
 * Id điểm vector là UUID suy ra từ (documentId, thứ tự đoạn) nên ingest lại sẽ ghi đè, không nhân đôi.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rag.ingest.enabled", havingValue = "true")
public class KnowledgeIngestService {

	static final int EMBED_BATCH = 16;
	static final int CHUNK_TOKENS = 500;
	static final int MIN_CHUNK_CHARS = 200;

	private final KnowledgeDocumentRepository repository;
	private final S3Service s3Service;
	private final VectorStore vectorStore;

	public void ingest(String documentId) {
		KnowledgeDocument doc = repository.findById(documentId).orElse(null);
		if (doc == null) {
			log.warn("Ingest bỏ qua: tài liệu {} không còn trong DB", documentId);
			return;
		}
		mark(doc, KnowledgeDocumentStatus.PROCESSING, null);
		try {
			byte[] bytes = s3Service.downloadFile(doc.getObjectKey());
			List<Document> chunks = split(parse(bytes, doc.getFileName()), doc);
			// xoá vector cũ (ingest lại) rồi ghi theo lô để mỗi lần gọi Ollama không quá nặng
			vectorStore.delete(byDocument(documentId));
			for (int i = 0; i < chunks.size(); i += EMBED_BATCH) {
				vectorStore.add(chunks.subList(i, Math.min(i + EMBED_BATCH, chunks.size())));
			}
			doc.setChunkCount(chunks.size());
			doc.setProcessedAt(Instant.now());
			mark(doc, KnowledgeDocumentStatus.READY, null);
			log.info("Ingest xong tài liệu {} ('{}'): {} đoạn", documentId, doc.getTitle(), chunks.size());
		} catch (Exception e) {
			log.error("Ingest lỗi tài liệu {}: {}", documentId, e.toString(), e);
			mark(doc, KnowledgeDocumentStatus.FAILED, abbreviate(e.toString(), 1000));
		}
	}

	public void deleteVectors(String documentId) {
		vectorStore.delete(byDocument(documentId));
		log.info("Đã xoá vector của tài liệu {}", documentId);
	}

	List<Document> parse(byte[] bytes, String fileName) {
		// Tika dựa vào tên file (đuôi) để đoán định dạng, nên phải giữ lại filename
		Resource resource = new ByteArrayResource(bytes) {
			@Override
			public String getFilename() {
				return fileName;
			}
		};
		return new TikaDocumentReader(resource).read();
	}

	List<Document> split(List<Document> parsed, KnowledgeDocument doc) {
		TokenTextSplitter splitter = TokenTextSplitter.builder()
				.withChunkSize(CHUNK_TOKENS)
				.withMinChunkSizeChars(MIN_CHUNK_CHARS)
				.withMinChunkLengthToEmbed(10)
				.withMaxNumChunks(5000)
				.build();
		List<Document> out = new ArrayList<>();
		int index = 0;
		for (Document chunk : splitter.apply(parsed)) {
			String text = chunk.getText();
			if (text == null || text.isBlank()) continue;
			Map<String, Object> metadata = new HashMap<>();
			metadata.put("documentId", doc.getId());
			metadata.put("title", doc.getTitle());
			metadata.put("fileName", doc.getFileName());
			metadata.put("chunkIndex", index);
			String pointId = UUID.nameUUIDFromBytes((doc.getId() + ":" + index).getBytes(StandardCharsets.UTF_8)).toString();
			out.add(Document.builder().id(pointId).text(text.strip()).metadata(metadata).build());
			index++;
		}
		return out;
	}

	static Filter.Expression byDocument(String documentId) {
		return new FilterExpressionBuilder().eq("documentId", documentId).build();
	}

	private void mark(KnowledgeDocument doc, KnowledgeDocumentStatus status, String error) {
		doc.setStatus(status);
		doc.setErrorMessage(error);
		doc.setUpdatedAt(Instant.now());
		repository.save(doc);
	}

	static String abbreviate(String s, int max) {
		if (s == null) return null;
		return s.length() <= max ? s : s.substring(0, max - 1) + "…";
	}
}
