package fit.iuh.se.hsapplication.controller.chat;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.request.SubmitContinuationDecisionRequest;
import fit.iuh.se.hschat.dto.response.ContinuationDecisionResponse;
import fit.iuh.se.hschat.entity.enums.ContinuationDecision;
import fit.iuh.se.hschat.service.authorization.EpisodeHealthRecordAuthorizationService;
import fit.iuh.se.hschat.service.continuation.QueueContinuationService;
import fit.iuh.se.hschat.service.finalsummary.ConsultationFinalSummaryService;
import fit.iuh.se.hschat.service.session.ConsultationSessionService;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class ConsultationContinuationControllerTest {

    private final QueueContinuationService continuations = mock(QueueContinuationService.class);
    private final ConsultationSessionController controller = new ConsultationSessionController(
            mock(ConsultationSessionService.class), mock(ConsultationFinalSummaryService.class),
            mock(EpisodeHealthRecordAuthorizationService.class), continuations);

    @Test
    void queryAndDecisionDeriveActorIdentityAndRoleFromAuthentication() {
        UserAuthentication member = UserAuthentication.builder().userId(10L).role(UserRole.MEMBER).build();
        ContinuationDecisionResponse current = mock(ContinuationDecisionResponse.class);
        ContinuationDecisionResponse decided = mock(ContinuationDecisionResponse.class);
        SubmitContinuationDecisionRequest request =
                new SubmitContinuationDecisionRequest(ContinuationDecision.CONTINUE);
        when(continuations.getCurrent(10L, UserRole.MEMBER, 50L)).thenReturn(current);
        when(continuations.decide(10L, UserRole.MEMBER, 50L, 0, request)).thenReturn(decided);

        assertSame(current, controller.getCurrentContinuation(member, 50L).getData());
        assertSame(decided, controller.submitContinuationDecision(member, 50L, 0, request).getData());
    }
}
