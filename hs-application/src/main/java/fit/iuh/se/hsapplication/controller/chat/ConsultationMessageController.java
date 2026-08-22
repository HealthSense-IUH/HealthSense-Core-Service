package fit.iuh.se.hsapplication.controller.chat;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.request.MarkConsultationReadRequest;
import fit.iuh.se.hschat.dto.request.SendConsultationMessageRequest;
import fit.iuh.se.hschat.dto.response.ConsultationMessageResponse;
import fit.iuh.se.hschat.dto.response.ConsultationParticipantResponse;
import fit.iuh.se.hschat.service.message.ConsultationMessageService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/consultation-sessions/{sessionId}/messages")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConsultationMessageController {

    ConsultationMessageService consultationMessageService;
    SimpMessagingTemplate messagingTemplate;

    @NonFinal
    @Value("${app.websocket.topic-prefix}")
    String topicPrefix;

    @PostMapping
    public ApiResponse<ConsultationMessageResponse> sendMessage(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long sessionId,
            @RequestBody @Valid SendConsultationMessageRequest request) {
        ConsultationMessageResponse response = consultationMessageService.sendMessage(currentUser.getUserId(), sessionId, request);
        messagingTemplate.convertAndSend(
                topicPrefix + "/consultation-sessions/" + sessionId,
                new ApiResponse<>(response)
        );
        return new ApiResponse<>(response);
    }

    @GetMapping
    public ApiResponse<PageResponse<ConsultationMessageResponse>> getMessages(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long sessionId,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "30") @Min(1) int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return new ApiResponse<>(consultationMessageService.getMessages(currentUser.getUserId(), sessionId, pageable));
    }

    @GetMapping("/before/{beforeMessageId}")
    public ApiResponse<PageResponse<ConsultationMessageResponse>> getMessagesBefore(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long sessionId,
            @PathVariable String beforeMessageId,
            @RequestParam(name = "page", defaultValue = "1") @Min(1) int page,
            @RequestParam(name = "size", defaultValue = "30") @Min(1) int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return new ApiResponse<>(consultationMessageService.getMessagesBefore(
                currentUser.getUserId(),
                sessionId,
                beforeMessageId,
                pageable
        ));
    }

    @PatchMapping("/read")
    public ApiResponse<ConsultationParticipantResponse> markAsRead(
            @AuthenticationPrincipal UserAuthentication currentUser,
            @PathVariable Long sessionId,
            @RequestBody @Valid MarkConsultationReadRequest request) {
        return new ApiResponse<>(consultationMessageService.markAsRead(currentUser.getUserId(), sessionId, request));
    }
}
