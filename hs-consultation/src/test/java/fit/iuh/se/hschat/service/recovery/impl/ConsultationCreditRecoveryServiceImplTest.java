package fit.iuh.se.hschat.service.recovery.impl;

import fit.iuh.se.hsbilling.dto.*;
import fit.iuh.se.hsbilling.entity.enums.CreditReservationStatus;
import fit.iuh.se.hsbilling.service.ConsultationCreditService;
import fit.iuh.se.hschat.entity.ConsultationRequest;
import fit.iuh.se.hschat.entity.enums.ConsultationRequestStatus;
import fit.iuh.se.hschat.repository.ConsultationRequestRepository;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ConsultationCreditRecoveryServiceImplTest {
    final ConsultationRequestRepository requests=mock(ConsultationRequestRepository.class);
    final ConsultationCreditService credits=mock(ConsultationCreditService.class);
    final ConsultationCreditRecoveryServiceImpl service=new ConsultationCreditRecoveryServiceImpl(requests,credits);

    @Test void releasesOnlyBoundedTerminalCandidatesAndRetryIsSafe() {
        var request=ConsultationRequest.builder().id(10L).memberId(20L)
                .status(ConsultationRequestStatus.CANCELLED).build();
        when(requests.findTerminalPaidRequestIds(anyCollection(),any(Pageable.class))).thenReturn(List.of(10L));
        when(requests.findFulfilledPaidRequestIds(any(Pageable.class))).thenReturn(List.of());
        when(requests.findByIdForUpdate(10L)).thenReturn(Optional.of(request));
        when(credits.inspectReservation(20L,10L)).thenReturn(new CreditReservationResponse(10L,null,1,
                CreditReservationStatus.HELD,new CreditWalletResponse(1,1,0)));
        assertEquals(1,service.recover(1L,UserRole.ADMIN,10).released());
        verify(credits).release(20L,10L,"recovery:terminal-request:CANCELLED");
    }

    @Test void activeRequestsAreNeverScannedOrReleased() {
        when(requests.findTerminalPaidRequestIds(anyCollection(),any(Pageable.class))).thenReturn(List.of());
        when(requests.findFulfilledPaidRequestIds(any(Pageable.class))).thenReturn(List.of());
        assertEquals(0,service.recover(1L,UserRole.SUPER_ADMIN,10).examined());
        verifyNoInteractions(credits);
    }
}
