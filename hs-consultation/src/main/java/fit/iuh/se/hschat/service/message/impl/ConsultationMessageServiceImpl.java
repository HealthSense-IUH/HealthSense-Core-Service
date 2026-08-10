package fit.iuh.se.hschat.service.message.impl;

import fit.iuh.se.hschat.dto.request.MarkConsultationReadRequest;
import fit.iuh.se.hschat.dto.request.SendConsultationMessageRequest;
import fit.iuh.se.hschat.dto.response.ConsultationMessageResponse;
import fit.iuh.se.hschat.dto.response.ConsultationParticipantResponse;
import fit.iuh.se.hschat.entity.ConsultationMessage;
import fit.iuh.se.hschat.entity.ConsultationParticipant;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.ConsultationMessageType;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.mapper.ConsultationMapper;
import fit.iuh.se.hschat.repository.ConsultationMessageRepository;
import fit.iuh.se.hschat.repository.ConsultationParticipantRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.service.message.ConsultationMessageService;
import fit.iuh.se.hschat.service.message.SupportHoursPolicy;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConsultationMessageServiceImpl implements ConsultationMessageService {

    ConsultationMessageRepository messageRepository;
    ConsultationSessionRepository sessionRepository;
    ConsultationParticipantRepository participantRepository;
    ConsultationMapper mapper;
    SupportHoursPolicy supportHoursPolicy;

    @Override
    @Transactional
    public ConsultationMessageResponse sendMessage(Long senderId, Long sessionId, SendConsultationMessageRequest request) {
        ConsultationSession session = getSessionForSending(sessionId);
        ConsultationParticipant participant = getActiveParticipant(sessionId, senderId);
        validateMessagePayload(request);
        validateSupportHours(session, participant);

        if (request.getClientMessageId() != null) {
            return messageRepository
                    .findBySessionIdAndSenderIdAndClientMessageId(sessionId, senderId, request.getClientMessageId())
                    .map(mapper::toMessageResponse)
                    .orElseGet(() -> createMessage(session, participant, request));
        }

        return createMessage(session, participant, request);
    }

    @Override
    public PageResponse<ConsultationMessageResponse> getMessages(Long userId, Long sessionId, Pageable pageable) {
        ensureCanAccessSession(sessionId, userId);

        List<ConsultationMessageResponse> messages = messageRepository
                .findBySessionIdAndActiveTrueOrderByCreatedAtDesc(sessionId, pageable)
                .stream()
                .map(mapper::toMessageResponse)
                .toList();

        return new PageResponse<>(new PageImpl<>(messages, pageable, messages.size()));
    }

    @Override
    public PageResponse<ConsultationMessageResponse> getMessagesBefore(
            Long userId,
            Long sessionId,
            String beforeMessageId,
            Pageable pageable
    ) {
        ensureCanAccessSession(sessionId, userId);

        ConsultationMessage beforeMessage = messageRepository.findById(beforeMessageId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_MESSAGE_NOT_FOUND));

        if (!beforeMessage.getSessionId().equals(sessionId))
            throw new AppException(ErrorCode.CONSULTATION_MESSAGE_NOT_FOUND);

        List<ConsultationMessageResponse> messages = messageRepository
                .findBySessionIdAndActiveTrueAndCreatedAtBeforeOrderByCreatedAtDesc(
                        sessionId,
                        beforeMessage.getCreatedAt(),
                        pageable
                )
                .stream()
                .map(mapper::toMessageResponse)
                .toList();

        return new PageResponse<>(new PageImpl<>(messages, pageable, messages.size()));
    }

    @Override
    @Transactional
    public ConsultationParticipantResponse markAsRead(
            Long userId,
            Long sessionId,
            MarkConsultationReadRequest request
    ) {
        ConsultationParticipant participant = getActiveParticipant(sessionId, userId);

        ConsultationMessage message = messageRepository.findById(request.getLastReadMessageId())
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_MESSAGE_NOT_FOUND));

        if (!message.getSessionId().equals(sessionId))
            throw new AppException(ErrorCode.CONSULTATION_MESSAGE_NOT_FOUND);

        participant.setLastReadMessageId(message.getId());
        participant.setLastReadAt(message.getCreatedAt());
        participant = participantRepository.save(participant);

        return mapper.toParticipantResponse(participant);
    }

    private ConsultationMessageResponse createMessage(
            ConsultationSession session,
            ConsultationParticipant participant,
            SendConsultationMessageRequest request
    ) {
        Instant now = Instant.now();
        ConsultationMessage message = ConsultationMessage.builder()
                .sessionId(session.getId())
                .senderId(participant.getUserId())
                .senderRole(participant.getRole())
                .type(request.getType())
                .content(request.getContent())
                .attachmentUrl(request.getAttachmentUrl())
                .attachmentName(request.getAttachmentName())
                .attachmentSize(request.getAttachmentSize())
                .attachmentContentType(request.getAttachmentContentType())
                .clientMessageId(request.getClientMessageId())
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        message = messageRepository.save(message);

        session.setLastMessageId(message.getId());
        session.setLastMessagePreview(buildPreview(message));
        session.setLastMessageAt(message.getCreatedAt());
        sessionRepository.save(session);

        return mapper.toMessageResponse(message);
    }

    private ConsultationSession getSessionForSending(Long sessionId) {
        ConsultationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_NOT_FOUND));

        if (session.getStatus() != ConsultationStatus.ACTIVE)
            throw new AppException(ErrorCode.CONSULTATION_NOT_ACTIVE);

        Instant supportEndsAt = session.getSupportEndsAt() == null ? session.getEndsAt() : session.getSupportEndsAt();
        if (supportEndsAt != null && supportEndsAt.isBefore(Instant.now()))
            throw new AppException(ErrorCode.CONSULTATION_NOT_ACTIVE);

        return session;
    }

    private void ensureCanAccessSession(Long sessionId, Long userId) {
        ConsultationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_NOT_FOUND));
        if (session.getStatus() != ConsultationStatus.ACTIVE && session.getStatus() != ConsultationStatus.COMPLETED)
            throw new AppException(ErrorCode.CONSULTATION_NOT_ACTIVE);

        if (!participantRepository.existsBySessionIdAndUserIdAndActiveTrue(sessionId, userId))
            throw new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED);
    }

    private ConsultationParticipant getActiveParticipant(Long sessionId, Long userId) {
        return participantRepository.findBySessionIdAndUserIdAndActiveTrue(sessionId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.CONSULTATION_ACCESS_DENIED));
    }

    private void validateMessagePayload(SendConsultationMessageRequest request) {
        if (request.getType() == ConsultationMessageType.TEXT && isBlank(request.getContent())) {
            throw new AppException(ErrorCode.INVALID_PARAMETER, "Text message content is required");
        }

        if ((request.getType() == ConsultationMessageType.IMAGE || request.getType() == ConsultationMessageType.FILE)
                && isBlank(request.getAttachmentUrl())) {
            throw new AppException(ErrorCode.INVALID_PARAMETER, "Attachment URL is required");
        }
    }

    private void validateSupportHours(ConsultationSession session, ConsultationParticipant participant) {
        if (!supportHoursPolicy.canSendNow(session, participant.getRole()))
            throw new AppException(ErrorCode.CONSULTATION_NOT_ACTIVE, "Member can send messages only during the session support hours");
    }

    private String buildPreview(ConsultationMessage message) {
        if (message.getType() == ConsultationMessageType.TEXT && message.getContent() != null) {
            return message.getContent().length() > 120
                    ? message.getContent().substring(0, 120)
                    : message.getContent();
        }

        return switch (message.getType()) {
            case IMAGE -> "[Image]";
            case FILE -> "[File]";
            case SYSTEM -> message.getContent();
            default -> "";
        };
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
