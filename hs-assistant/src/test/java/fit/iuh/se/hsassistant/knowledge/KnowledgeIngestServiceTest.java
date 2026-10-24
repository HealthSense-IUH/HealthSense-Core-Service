package fit.iuh.se.hsassistant.knowledge;

import fit.iuh.se.hsassistant.entity.KnowledgeDocument;
import fit.iuh.se.hsassistant.entity.KnowledgeDocumentStatus;
import fit.iuh.se.hsassistant.repository.KnowledgeDocumentRepository;
import fit.iuh.se.hsshared.service.s3.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pipeline ingest với file text thật (Tika + TokenTextSplitter), còn MinIO và Qdrant được mock:
 * kiểm tra đoạn được gắn metadata documentId/title, id điểm là UUID ổn định, và trạng thái tài liệu.
 */
class KnowledgeIngestServiceTest {

	private KnowledgeDocumentRepository repository;
	private S3Service s3Service;
	private VectorStore vectorStore;
	private KnowledgeIngestService service;
	private KnowledgeDocument doc;

	@BeforeEach
	void setUp() {
		repository = mock(KnowledgeDocumentRepository.class);
		s3Service = mock(S3Service.class);
		vectorStore = mock(VectorStore.class);
		service = new KnowledgeIngestService(repository, s3Service, vectorStore);
		doc = KnowledgeDocument.builder()
				.id("d1").title("Hướng dẫn sử dụng").fileName("huong-dan.txt").contentType("text/plain")
				.sizeBytes(10).objectKey("documents/knowledge/d1/huong-dan.txt")
				.status(KnowledgeDocumentStatus.PENDING).createdAt(Instant.now()).updatedAt(Instant.now())
				.build();
		when(repository.findById("d1")).thenReturn(Optional.of(doc));
	}

	@Test
	void ingestsTextFileIntoVectorStoreAndMarksReady() {
		String text = ("Rung nhĩ là rối loạn nhịp tim thường gặp. Vòng đeo HealthSense đo PPG mỗi phút và cảnh báo "
				+ "khi nghi ngờ rung nhĩ. Người dùng nên đo ở tư thế ngồi yên. ").repeat(40);
		when(s3Service.downloadFile(doc.getObjectKey())).thenReturn(text.getBytes(StandardCharsets.UTF_8));

		service.ingest("d1");

		verify(vectorStore).delete(any(Filter.Expression.class));
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
		verify(vectorStore).add(captor.capture());
		List<Document> chunks = captor.getAllValues().stream().flatMap(List::stream).toList();
		assertThat(chunks).isNotEmpty();
		assertThat(chunks.get(0).getMetadata())
				.containsEntry("documentId", "d1")
				.containsEntry("title", "Hướng dẫn sử dụng")
				.containsEntry("chunkIndex", 0);
		assertThat(chunks.get(0).getId()).matches("[0-9a-f-]{36}");
		assertThat(chunks.get(0).getText()).contains("Rung nhĩ");

		assertThat(doc.getStatus()).isEqualTo(KnowledgeDocumentStatus.READY);
		assertThat(doc.getChunkCount()).isEqualTo(chunks.size());
		assertThat(doc.getProcessedAt()).isNotNull();
		assertThat(doc.getErrorMessage()).isNull();
	}

	@Test
	void marksFailedWhenDownloadFails() {
		when(s3Service.downloadFile(doc.getObjectKey())).thenThrow(new RuntimeException("MinIO down"));

		service.ingest("d1");

		verify(vectorStore, never()).add(anyList());
		assertThat(doc.getStatus()).isEqualTo(KnowledgeDocumentStatus.FAILED);
		assertThat(doc.getErrorMessage()).contains("MinIO down");
	}

	@Test
	void ignoresUnknownDocument() {
		when(repository.findById("missing")).thenReturn(Optional.empty());

		service.ingest("missing");

		verify(vectorStore, never()).add(anyList());
		verify(s3Service, never()).downloadFile(any());
	}
}
