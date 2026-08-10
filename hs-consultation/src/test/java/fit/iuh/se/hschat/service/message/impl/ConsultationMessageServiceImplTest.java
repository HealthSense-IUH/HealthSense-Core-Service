package fit.iuh.se.hschat.service.message.impl;

import fit.iuh.se.hschat.dto.request.SendConsultationMessageRequest;
import fit.iuh.se.hschat.entity.ConsultationMessage;
import fit.iuh.se.hschat.entity.ConsultationParticipant;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.ConsultationMessageType;
import fit.iuh.se.hschat.entity.enums.ConsultationParticipantRole;
import fit.iuh.se.hschat.entity.enums.ConsultationStatus;
import fit.iuh.se.hschat.mapper.ConsultationMapper;
import fit.iuh.se.hschat.repository.ConsultationMessageRepository;
import fit.iuh.se.hschat.repository.ConsultationParticipantRepository;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hschat.service.message.SupportHoursPolicy;
import fit.iuh.se.hsshared.advice.entity.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultationMessageServiceImplTest {

    @Mock
    ConsultationMessageRepository messageRepository;
    @Mock
    ConsultationSessionRepository sessionRepository;
    @Mock
    ConsultationParticipantRepository participantRepository;
    @Mock
    ConsultationMapper mapper;
    @Mock
    SupportHoursPolicy supportHoursPolicy;

    ConsultationMessageServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ConsultationMessageServiceImpl(
                messageRepository,
                sessionRepository,
                participantRepository,
                mapper,
                supportHoursPolicy
        );
    }

    @Test
    void scheduledSessionDoesNotAllowMessageRead() {
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(session(ConsultationStatus.SCHEDULED)));

        assertThrows(AppException.class, () -> service.getMessages(1L, 100L, PageRequest.of(0, 10)));
        verify(messageRepository, never()).findBySessionIdAndActiveTrueOrderByCreatedAtDesc(anyLong(), any());
    }

    @Test
    void completedSessionAllowsReadOnlyMessages() {
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(session(ConsultationStatus.COMPLETED)));
        when(participantRepository.existsBySessionIdAndUserIdAndActiveTrue(100L, 1L)).thenReturn(true);
        when(messageRepository.findBySessionIdAndActiveTrueOrderByCreatedAtDesc(eq(100L), any())).thenReturn(List.of());

        service.getMessages(1L, 100L, PageRequest.of(0, 10));

        verify(messageRepository).findBySessionIdAndActiveTrueOrderByCreatedAtDesc(eq(100L), any());
    }

    @Test
    void completedSessionRejectsSending() {
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(session(ConsultationStatus.COMPLETED)));

        assertThrows(AppException.class, () -> service.sendMessage(1L, 100L, textRequest()));
        verify(messageRepository, never()).save(any());
    }

    @Test
    void memberOutsideSupportHoursCannotSend() {
        ConsultationSession session = session(ConsultationStatus.ACTIVE);
        ConsultationParticipant participant = participant(1L, ConsultationParticipantRole.MEMBER);
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(session));
        when(participantRepository.findBySessionIdAndUserIdAndActiveTrue(100L, 1L)).thenReturn(Optional.of(participant));
        when(supportHoursPolicy.canSendNow(session, ConsultationParticipantRole.MEMBER)).thenReturn(false);

        assertThrows(AppException.class, () -> service.sendMessage(1L, 100L, textRequest()));
        verify(messageRepository, never()).save(any());
    }

    @Test
    void doctorOutsideSupportHoursCanSendWhileActive() {
        ConsultationSession session = session(ConsultationStatus.ACTIVE);
        ConsultationParticipant participant = participant(2L, ConsultationParticipantRole.DOCTOR);
        when(sessionRepository.findById(100L)).thenReturn(Optional.of(session));
        when(participantRepository.findBySessionIdAndUserIdAndActiveTrue(100L, 2L)).thenReturn(Optional.of(participant));
        when(supportHoursPolicy.canSendNow(session, ConsultationParticipantRole.DOCTOR)).thenReturn(true);
        when(messageRepository.save(any(ConsultationMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.sendMessage(2L, 100L, textRequest());

        verify(messageRepository).save(any(ConsultationMessage.class));
        verify(sessionRepository).save(session);
    }

    private ConsultationSession session(ConsultationStatus status) {
        return ConsultationSession.builder()
                .id(100L)
                .memberId(1L)
                .doctorId(2L)
                .status(status)
                .startedAt(Instant.now().minusSeconds(60))
                .endsAt(Instant.now().plusSeconds(3600))
                .supportEndsAt(Instant.now().plusSeconds(3600))
                .build();
    }

    private ConsultationParticipant participant(Long userId, ConsultationParticipantRole role) {
        return ConsultationParticipant.builder()
                .sessionId(100L)
                .userId(userId)
                .role(role)
                .active(true)
                .build();
    }

    private SendConsultationMessageRequest textRequest() {
        SendConsultationMessageRequest request = new SendConsultationMessageRequest();
        request.setType(ConsultationMessageType.TEXT);
        request.setContent("Hello");
        return request;
    }
}
