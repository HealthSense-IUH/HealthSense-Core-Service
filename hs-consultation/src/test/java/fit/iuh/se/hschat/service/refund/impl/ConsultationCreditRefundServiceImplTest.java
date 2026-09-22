package fit.iuh.se.hschat.service.refund.impl;

import fit.iuh.se.hsbilling.service.CreditAdministrationService;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.ConsultationCreditPolicy;
import fit.iuh.se.hschat.repository.ConsultationSessionRepository;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsultationCreditRefundServiceImplTest {
    final ConsultationSessionRepository sessions=mock(ConsultationSessionRepository.class);
    final CreditAdministrationService credits=mock(CreditAdministrationService.class);
    final ConsultationCreditRefundServiceImpl service=new ConsultationCreditRefundServiceImpl(sessions,credits);

    @Test void verifiesPaidSessionSnapshotBeforeCallingBilling() {
        var session=ConsultationSession.builder().id(10L).memberId(20L)
                .creditPolicy(ConsultationCreditPolicy.PER_SESSION_CONFIRM_V2).creditCost(1L).build();
        when(sessions.findById(10L)).thenReturn(Optional.of(session));
        service.refund(1L,UserRole.ADMIN,10L,"care incident","refund-10");
        verify(credits).refundCapturedSession(1L,UserRole.ADMIN,20L,10L,1L,"care incident","refund-10");
    }

    @Test void rejectsFreeSessionAndNonFinancialRoles() {
        when(sessions.findById(10L)).thenReturn(Optional.of(ConsultationSession.builder().id(10L).memberId(20L)
                .creditPolicy(ConsultationCreditPolicy.FREE_EXISTING).creditCost(0L).build()));
        assertEquals(ErrorCode.CREDIT_REFUND_NOT_ELIGIBLE,
                assertThrows(AppException.class,()->service.refund(1L,UserRole.ADMIN,10L,"reason","key")).getErrorCode());
        assertEquals(ErrorCode.ACCESS_DENIED,
                assertThrows(AppException.class,()->service.refund(1L,UserRole.CARE_COORDINATOR,10L,"reason","key")).getErrorCode());
        verifyNoInteractions(credits);
    }
}
