package fit.iuh.se.hsassistant.service;

import fit.iuh.se.hsassistant.dto.AssistantUser;
import fit.iuh.se.hsassistant.knowledge.KnowledgeAssistantTools;
import fit.iuh.se.hsassistant.knowledge.KnowledgeSearchService;
import fit.iuh.se.hschat.service.carehistory.CareHistoryService;
import fit.iuh.se.hschat.service.session.ConsultationSessionService;
import fit.iuh.se.hshealthrecord.service.user.HealthRecordService;
import fit.iuh.se.hshealthrecord.service.workout.WorkoutService;
import fit.iuh.se.hsnutrition.service.DietPrescriptionService;
import fit.iuh.se.hsuser.dto.response.UserResponse;
import fit.iuh.se.hsuser.service.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Lõi của trợ lý: ghép system prompt (StringTemplate trong resources/prompts), bộ nhớ hội thoại,
 * tool đọc dữ liệu người dùng, rồi gọi OpenAI ở chế độ streaming và trả về từng mẩu chữ (Flux).
 */
@Slf4j
@Service
public class AssistantChatService {

	private final ChatClient chatClient;
	private final AssistantPageCatalog pageCatalog;
	private final AssistantConversationService conversations;
	private final UserService userService;
	private final HealthRecordService healthRecordService;
	private final WorkoutService workoutService;
	private final ConsultationSessionService consultationSessionService;
	private final CareHistoryService careHistoryService;
	private final DietPrescriptionService dietPrescriptionService;
	private final Resource systemPrompt;
	/** Có bean khi app.rag.enabled=true; dev thường tắt vì không với tới Qdrant/Ollama. */
	private final KnowledgeSearchService knowledgeSearch;

	public AssistantChatService(ChatClient.Builder chatClientBuilder,
								ChatMemory chatMemory,
								AssistantPageCatalog pageCatalog,
								AssistantConversationService conversations,
								UserService userService,
								HealthRecordService healthRecordService,
								WorkoutService workoutService,
								ConsultationSessionService consultationSessionService,
								CareHistoryService careHistoryService,
								DietPrescriptionService dietPrescriptionService,
								ObjectProvider<KnowledgeSearchService> knowledgeSearch,
								@Value("classpath:prompts/assistant-system.st") Resource systemPrompt) {
		this.chatClient = chatClientBuilder
				.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
				.build();
		this.pageCatalog = pageCatalog;
		this.conversations = conversations;
		this.userService = userService;
		this.healthRecordService = healthRecordService;
		this.workoutService = workoutService;
		this.consultationSessionService = consultationSessionService;
		this.careHistoryService = careHistoryService;
		this.dietPrescriptionService = dietPrescriptionService;
		this.systemPrompt = systemPrompt;
		this.knowledgeSearch = knowledgeSearch.getIfAvailable();
	}

	/**
	 * Trả lời một lượt hỏi theo thời gian thực. Mỗi phần tử của Flux là một mẩu chữ nối tiếp nhau;
	 * khi Flux hoàn tất thì hội thoại được cập nhật tiêu đề và thời điểm.
	 */
	static final String KNOWLEDGE_RULE = "- Với câu hỏi về kiến thức, hướng dẫn sử dụng, chính sách, hoặc rung nhĩ/sức khỏe tim mạch nói chung: "
			+ "gọi tool searchKnowledge trước, trả lời dựa trên các đoạn tìm được và ghi nguồn bằng tên tài liệu "
			+ "(ví dụ: *Nguồn: Hướng dẫn sử dụng*). Nếu không tìm thấy đoạn phù hợp, nói rõ kho tài liệu chưa có thông tin "
			+ "rồi mới trả lời theo kiến thức chung.";

	public Flux<String> stream(AssistantUser user, String conversationId, String message) {
		conversations.requireOwned(conversationId, user.userId());
		UserResponse profile = userService.getProfile(user.userId());
		String displayName = profile.getDisplayName() != null ? profile.getDisplayName() : profile.getEmail();
		String timezone = profile.getTimezone() != null ? profile.getTimezone() : user.timezone();
		AssistantUser effective = AssistantUser.of(user.userId(), user.role(), user.language(), timezone);
		String today = LocalDate.now(ZoneId.of(effective.timezone()))
				.format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new Locale("vi", "VN")));

		MemberAssistantTools memberTools = new MemberAssistantTools(effective, userService, healthRecordService,
				workoutService, consultationSessionService, careHistoryService, dietPrescriptionService);
		Object[] tools = knowledgeSearch != null
				? new Object[] {memberTools, new KnowledgeAssistantTools(knowledgeSearch)}
				: new Object[] {memberTools};

		return chatClient.prompt()
				.system(s -> s.text(systemPrompt)
						.param("displayName", displayName)
						.param("role", effective.role() == null ? "UNKNOWN" : effective.role().name())
						.param("today", today)
						.param("timezone", effective.timezone())
						.param("language", "en".equals(effective.language()) ? "English" : "tiếng Việt")
						.param("pages", pageCatalog.renderFor(effective.role()))
						.param("knowledgeRule", knowledgeSearch != null ? KNOWLEDGE_RULE : ""))
				.user(message)
				.tools(tools)
				.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
				.stream()
				.content()
				.doOnComplete(() -> conversations.touch(conversationId, user.userId(), message))
				.doOnError(e -> log.warn("Assistant stream lỗi (user {}, conversation {}): {}",
						user.userId(), conversationId, e.toString()));
	}
}
