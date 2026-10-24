package fit.iuh.se.hsapplication.controller.assistant;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hsapplication.dto.request.AssistantChatRequest;
import fit.iuh.se.hsassistant.dto.AssistantConversationResponse;
import fit.iuh.se.hsassistant.dto.AssistantMessageResponse;
import fit.iuh.se.hsassistant.dto.AssistantUser;
import fit.iuh.se.hsassistant.service.AssistantChatService;
import fit.iuh.se.hsassistant.service.AssistantConversationService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Trợ lý AI. Lượt hỏi trả lời bằng Server-Sent Events: mỗi mẩu chữ là một sự kiện "delta",
 * kết thúc bằng "done"; lỗi giữa chừng gửi sự kiện "error" rồi đóng. Mọi vai trò đã đăng nhập đều dùng được,
 * tool đọc dữ liệu tự giới hạn theo vai trò.
 */
@Slf4j
@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AssistantController {

	static final long STREAM_TIMEOUT_MS = 180_000L;

	AssistantConversationService conversationService;
	AssistantChatService chatService;

	@GetMapping("/conversations")
	public ApiResponse<List<AssistantConversationResponse>> listConversations(
			@AuthenticationPrincipal UserAuthentication currentUser) {
		return new ApiResponse<>(conversationService.listMine(currentUser.getUserId()));
	}

	@PostMapping("/conversations")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<AssistantConversationResponse> createConversation(
			@AuthenticationPrincipal UserAuthentication currentUser) {
		return new ApiResponse<>(conversationService.create(currentUser.getUserId()));
	}

	@GetMapping("/conversations/{conversationId}/messages")
	public ApiResponse<List<AssistantMessageResponse>> messages(
			@AuthenticationPrincipal UserAuthentication currentUser,
			@PathVariable String conversationId) {
		return new ApiResponse<>(conversationService.messages(conversationId, currentUser.getUserId()));
	}

	@DeleteMapping("/conversations/{conversationId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteConversation(@AuthenticationPrincipal UserAuthentication currentUser,
								   @PathVariable String conversationId) {
		conversationService.delete(conversationId, currentUser.getUserId());
	}

	@PostMapping(value = "/conversations/{conversationId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter stream(@AuthenticationPrincipal UserAuthentication currentUser,
							 @PathVariable String conversationId,
							 @Valid @RequestBody AssistantChatRequest request,
							 @RequestHeader(value = "lang", required = false) String lang) {
		AssistantUser user = AssistantUser.of(currentUser.getUserId(), currentUser.getRole(), lang, null);
		SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MS);

		Disposable subscription = chatService.stream(user, conversationId, request.message())
				.subscribe(
						chunk -> send(emitter, "delta", Map.of("text", chunk)),
						error -> {
							log.warn("Assistant SSE lỗi: {}", error.toString());
							send(emitter, "error", Map.of("message", "Cara đang gặp sự cố, vui lòng thử lại sau."));
							emitter.complete();
						},
						() -> {
							send(emitter, "done", Map.of());
							emitter.complete();
						});

		emitter.onTimeout(subscription::dispose);
		emitter.onError(e -> subscription.dispose());
		emitter.onCompletion(subscription::dispose);
		return emitter;
	}

	private static void send(SseEmitter emitter, String event, Object payload) {
		try {
			emitter.send(SseEmitter.event().name(event).data(payload, MediaType.APPLICATION_JSON));
		} catch (IOException | IllegalStateException e) {
			// Client đã ngắt kết nối: onCompletion/onError của emitter sẽ dọn subscription
			log.debug("Assistant SSE không gửi được sự kiện {}: {}", event, e.toString());
		}
	}
}
