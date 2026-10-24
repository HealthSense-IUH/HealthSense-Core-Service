package fit.iuh.se.hsassistant.knowledge;

import fit.iuh.se.hsassistant.dto.KnowledgeDocumentResponse;
import fit.iuh.se.hsassistant.dto.KnowledgeIngestMessage;
import fit.iuh.se.hsassistant.dto.KnowledgeSummary;
import fit.iuh.se.hsassistant.entity.KnowledgeDocument;
import fit.iuh.se.hsassistant.entity.KnowledgeDocumentStatus;
import fit.iuh.se.hsassistant.repository.KnowledgeDocumentRepository;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import fit.iuh.se.hsshared.service.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Quản lý tài liệu tri thức (admin): tải lên MinIO/S3, lưu metadata, rồi đẩy message để worker ingest
 * (instance cloud có Ollama) tách đoạn + embedding. Việc ghi/xoá vector không làm ở đây để dev
 * không cần với tới Ollama/Qdrant.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeDocumentService {

	static final long MAX_SIZE_BYTES = 20L * 1024 * 1024;
	static final Set<String> ALLOWED_EXTENSIONS = Set.of(
			"pdf", "docx", "doc", "txt", "md", "markdown", "html", "htm", "csv", "xlsx", "pptx", "rtf", "odt");

	private final KnowledgeDocumentRepository repository;
	private final S3Service s3Service;
	private final RabbitTemplate rabbitTemplate;

	@Value("${app.rabbitmq.exchange:health.record.exchange}")
	private String exchange;

	@Value("${app.rabbitmq.routing-key.ingest:document.ingest.routing}")
	private String ingestRoutingKey;

	public KnowledgeDocumentResponse upload(MultipartFile file, String title, Long uploaderId) {
		if (file == null || file.isEmpty()) {
			throw AppException.of(ErrorCode.INVALID_ARGUMENT, "detail.knowledge-file-required");
		}
		if (file.getSize() > MAX_SIZE_BYTES) {
			throw AppException.of(ErrorCode.INVALID_ARGUMENT, "detail.knowledge-file-too-large");
		}
		String originalName = StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), "document"));
		if (!ALLOWED_EXTENSIONS.contains(extensionOf(originalName))) {
			throw AppException.of(ErrorCode.INVALID_ARGUMENT, "detail.knowledge-file-type-unsupported");
		}

		String id = UUID.randomUUID().toString();
		String safeName = originalName.replaceAll("[^\\p{L}\\p{N}._-]+", "_");
		String objectKey = S3Service.FOLDER_DOCUMENTS + "knowledge/" + id + "/" + safeName;
		String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
		try (InputStream in = file.getInputStream()) {
			s3Service.uploadFileDirect(objectKey, in, file.getSize(), contentType);
		} catch (IOException e) {
			throw new UncheckedIOException("Không đọc được file tải lên", e);
		}

		Instant now = Instant.now();
		KnowledgeDocument doc = KnowledgeDocument.builder()
				.id(id)
				.title(resolveTitle(title, originalName))
				.fileName(originalName)
				.contentType(contentType)
				.sizeBytes(file.getSize())
				.objectKey(objectKey)
				.status(KnowledgeDocumentStatus.PENDING)
				.chunkCount(0)
				.uploadedBy(uploaderId)
				.createdAt(now)
				.updatedAt(now)
				.build();
		// save() tự commit trước khi gửi message, để worker chắc chắn đọc được bản ghi
		repository.save(doc);
		requestIngest(doc);
		log.info("Knowledge document {} ('{}') uploaded by {}, status {}", id, doc.getTitle(), uploaderId, doc.getStatus());
		return KnowledgeDocumentResponse.from(doc);
	}

	public PageResponse<KnowledgeDocumentResponse> list(String keyword, Pageable pageable) {
		Page<KnowledgeDocument> page = StringUtils.hasText(keyword)
				? repository.findByTitleContainingIgnoreCaseOrFileNameContainingIgnoreCase(keyword.trim(), keyword.trim(), pageable)
				: repository.findAll(pageable);
		return new PageResponse<>(page.map(KnowledgeDocumentResponse::from));
	}

	public KnowledgeSummary summary() {
		long pending = repository.countByStatus(KnowledgeDocumentStatus.PENDING);
		long processing = repository.countByStatus(KnowledgeDocumentStatus.PROCESSING);
		return new KnowledgeSummary(
				repository.count(),
				repository.countByStatus(KnowledgeDocumentStatus.READY),
				pending + processing,
				repository.countByStatus(KnowledgeDocumentStatus.FAILED));
	}

	public KnowledgeDocumentResponse get(String id) {
		return KnowledgeDocumentResponse.from(require(id));
	}

	public KnowledgeDocumentResponse reingest(String id) {
		KnowledgeDocument doc = require(id);
		doc.setStatus(KnowledgeDocumentStatus.PENDING);
		doc.setErrorMessage(null);
		doc.setUpdatedAt(Instant.now());
		repository.save(doc);
		requestIngest(doc);
		return KnowledgeDocumentResponse.from(doc);
	}

	public void delete(String id) {
		KnowledgeDocument doc = require(id);
		repository.delete(doc);
		s3Service.deleteFile(doc.getObjectKey());
		// vector ở Qdrant do worker cloud xoá; không gửi được thì chỉ cảnh báo (vector mồ côi không ảnh hưởng tra cứu vì bản ghi đã mất)
		try {
			publish(id, KnowledgeIngestMessage.Action.DELETE);
		} catch (Exception e) {
			log.warn("Không gửi được yêu cầu xoá vector cho tài liệu {}: {}", id, e.toString());
		}
		log.info("Knowledge document {} deleted, vector cleanup requested", id);
	}

	private KnowledgeDocument require(String id) {
		return repository.findById(id)
				.orElseThrow(() -> AppException.of(ErrorCode.ENTITY_NOT_FOUND, "detail.knowledge-document-not-found"));
	}

	/** Gửi yêu cầu ingest; RabbitMQ không tới được thì đánh dấu FAILED kèm lý do để admin bấm "Ingest lại" sau. */
	private void requestIngest(KnowledgeDocument doc) {
		try {
			publish(doc.getId(), KnowledgeIngestMessage.Action.INGEST);
		} catch (Exception e) {
			log.error("Không gửi được yêu cầu ingest cho tài liệu {}: {}", doc.getId(), e.toString());
			doc.setStatus(KnowledgeDocumentStatus.FAILED);
			doc.setErrorMessage("Không gửi được yêu cầu ingest tới RabbitMQ: " + abbreviate(e.getMessage(), 300));
			doc.setUpdatedAt(Instant.now());
			repository.save(doc);
		}
	}

	private void publish(String documentId, KnowledgeIngestMessage.Action action) {
		KnowledgeIngestMessage message = KnowledgeIngestMessage.builder().documentId(documentId).action(action).build();
		rabbitTemplate.convertAndSend(exchange, ingestRoutingKey, message);
	}

	static String abbreviate(String s, int max) {
		if (s == null) return "";
		return s.length() <= max ? s : s.substring(0, max - 1) + "…";
	}

	static String extensionOf(String fileName) {
		int dot = fileName.lastIndexOf('.');
		return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
	}

	static String resolveTitle(String title, String fileName) {
		String t = StringUtils.hasText(title) ? title.trim() : fileName.replaceAll("\\.[^.]+$", "");
		return t.length() > 200 ? t.substring(0, 200) : t;
	}
}
