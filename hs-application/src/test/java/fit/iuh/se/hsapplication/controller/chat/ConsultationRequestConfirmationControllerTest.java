package fit.iuh.se.hsapplication.controller.chat;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.request.ConfirmConsultationRequest;
import fit.iuh.se.hschat.dto.response.ConsultationSessionResponse;
import fit.iuh.se.hschat.service.request.ConsultationRequestService;
import fit.iuh.se.hschat.service.session.QueueConsultationSessionService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsultationRequestConfirmationControllerTest {
    private final ConsultationRequestService requests = mock(ConsultationRequestService.class);
    private final QueueConsultationSessionService sessions = mock(QueueConsultationSessionService.class);
    private final ConsultationRequestController controller = new ConsultationRequestController(requests, sessions);

    @Test void owningMemberIdentityIsPassedToConfirmationService() {
        var authentication = UserAuthentication.builder().userId(12L).role(UserRole.MEMBER).build();
        var expected = ConsultationSessionResponse.builder().id(500L).build();
        when(sessions.confirmMember(12L, 11L, "offer-a")).thenReturn(expected);
        assertSame(expected, controller.confirmQueueConsultation(
                authentication, 11L, new ConfirmConsultationRequest("offer-a")).getData());
    }

    @Test void nonMemberCannotCallConfirmationService() {
        var authentication = UserAuthentication.builder().userId(20L).role(UserRole.DOCTOR).build();
        assertThrows(AppException.class, () -> controller.confirmQueueConsultation(
                authentication, 11L, new ConfirmConsultationRequest("offer-a")));
        verifyNoInteractions(sessions);
    }
}
