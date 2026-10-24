package fit.iuh.se.hsapplication.controller.admin;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsassistant.dto.KnowledgeDocumentResponse;
import fit.iuh.se.hsassistant.dto.KnowledgeSummary;
import fit.iuh.se.hsassistant.knowledge.KnowledgeDocumentService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Quản lý kho tri thức của trợ lý Cara (ADMIN / SUPER_ADMIN, gác ở SecurityConfig: /api/admin/knowledge/**).
 */
@RestController
@RequestMapping("/api/admin/knowledge")
@RequiredArgsConstructor
@Validated
public class KnowledgeManagementController {

	private final KnowledgeDocumentService knowledgeDocumentService;

	@GetMapping("/documents")
	public ApiResponse<PageResponse<KnowledgeDocumentResponse>> list(
			@RequestParam(name = "keyword", required = false) String keyword,
			@RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
			@RequestParam(name = "size", defaultValue = "10") @Min(1) int size) {
		Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		return new ApiResponse<>(knowledgeDocumentService.list(keyword, pageable));
	}

	@GetMapping("/documents/summary")
	public ApiResponse<KnowledgeSummary> summary() {
		return new ApiResponse<>(knowledgeDocumentService.summary());
	}

	@PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<KnowledgeDocumentResponse> upload(
			@AuthenticationPrincipal UserAuthentication currentUser,
			@RequestParam("file") MultipartFile file,
			@RequestParam(name = "title", required = false) String title) {
		return new ApiResponse<>(knowledgeDocumentService.upload(file, title, currentUser.getUserId()));
	}

	@GetMapping("/documents/{id}")
	public ApiResponse<KnowledgeDocumentResponse> get(@PathVariable String id) {
		return new ApiResponse<>(knowledgeDocumentService.get(id));
	}

	@PostMapping("/documents/{id}/reingest")
	public ApiResponse<KnowledgeDocumentResponse> reingest(@PathVariable String id) {
		return new ApiResponse<>(knowledgeDocumentService.reingest(id));
	}

	@DeleteMapping("/documents/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable String id) {
		knowledgeDocumentService.delete(id);
	}
}
