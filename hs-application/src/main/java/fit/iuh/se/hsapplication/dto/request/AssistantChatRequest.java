package fit.iuh.se.hsapplication.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Một lượt hỏi gửi vào hội thoại của trợ lý. */
public record AssistantChatRequest(@NotBlank @Size(max = 4000) String message) {
}
