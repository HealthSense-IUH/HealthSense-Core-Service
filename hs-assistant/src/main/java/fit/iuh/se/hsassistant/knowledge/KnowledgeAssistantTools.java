package fit.iuh.se.hsassistant.knowledge;

import fit.iuh.se.hsassistant.dto.KnowledgeHit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

/**
 * Tool cho trợ lý Cara tra cứu kho tài liệu (RAG). Không lọc theo người dùng vì kho tri thức là chung
 * (hướng dẫn, chính sách, kiến thức y khoa), không chứa dữ liệu cá nhân.
 */
@Slf4j
@RequiredArgsConstructor
public class KnowledgeAssistantTools {

	private final KnowledgeSearchService searchService;

	@Tool(description = """
			Tìm trong kho tài liệu của HealthSense (hướng dẫn sử dụng ứng dụng, chính sách, kiến thức về rung nhĩ \
			và sức khỏe tim mạch). Trả về các đoạn liên quan nhất kèm tên tài liệu nguồn. \
			Gọi tool này trước khi trả lời câu hỏi kiến thức hoặc hướng dẫn.""")
	public List<KnowledgeHit> searchKnowledge(
			@ToolParam(description = "Câu hỏi hoặc từ khoá cần tra cứu, giữ nguyên ngôn ngữ của người dùng") String query) {
		try {
			return searchService.search(query, 5);
		} catch (Exception e) {
			log.warn("searchKnowledge lỗi: {}", e.toString());
			return List.of();
		}
	}
}
