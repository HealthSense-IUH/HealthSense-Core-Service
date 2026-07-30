package fit.iuh.se.hsapplication.controller.chat;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.request.SendConsultationMessageRequest;
import fit.iuh.se.hschat.dto.response.ConsultationMessageResponse;
import fit.iuh.se.hschat.service.message.ConsultationMessageService;
import fit.iuh.se.hsshared.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConsultationMessageWebSocketController {

    ConsultationMessageService consultationMessageService;
    SimpMessagingTemplate messagingTemplate;

    @NonFinal
    @Value("${app.websocket.topic-prefix}")
    String topicPrefix;

    @MessageMapping("/consultation-sessions/{sessionId}/messages")
    public void sendMessage(
            Principal principal,
            @DestinationVariable Long sessionId,
            @Payload @Valid SendConsultationMessageRequest request) {
        UserAuthentication currentUser = (UserAuthentication) ((Authentication) principal).getPrincipal();
        ConsultationMessageResponse response = consultationMessageService.sendMessage(
                currentUser.getUserId(),
                sessionId,
                request
        );

        messagingTemplate.convertAndSend(
                topicPrefix + "/consultation-sessions/" + sessionId,
                new ApiResponse<>(response)
        );
    }
}
