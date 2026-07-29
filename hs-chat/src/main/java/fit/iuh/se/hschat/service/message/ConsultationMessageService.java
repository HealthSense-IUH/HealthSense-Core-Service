package fit.iuh.se.hschat.service.message;

import fit.iuh.se.hschat.dto.request.MarkConsultationReadRequest;
import fit.iuh.se.hschat.dto.request.SendConsultationMessageRequest;
import fit.iuh.se.hschat.dto.response.ConsultationMessageResponse;
import fit.iuh.se.hschat.dto.response.ConsultationParticipantResponse;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface ConsultationMessageService {

    ConsultationMessageResponse sendMessage(Long senderId, String sessionId, SendConsultationMessageRequest request);

    PageResponse<ConsultationMessageResponse> getMessages(Long userId, String sessionId, Pageable pageable);

    PageResponse<ConsultationMessageResponse> getMessagesBefore(
            Long userId,
            String sessionId,
            String beforeMessageId,
            Pageable pageable
    );

    ConsultationParticipantResponse markAsRead(
            Long userId,
            String sessionId,
            MarkConsultationReadRequest request
    );
}
