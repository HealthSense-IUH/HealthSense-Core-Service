package fit.iuh.se.hsbilling.service;

import fit.iuh.se.hsbilling.dto.*;
import fit.iuh.se.hsshared.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ConsultationCreditService {
    List<CreditPackageResponse> getPackages(Long memberId);
    CreditWalletResponse getWallet(Long memberId);
    PageResponse<CreditLedgerResponse> getLedger(Long memberId, Pageable pageable);

    // Internal use only: caller verifies purchase evidence or consultation ownership.
    // Mutations join the caller's PostgreSQL transaction; there is no public funding API.
    CreditWalletResponse credit(Long memberId, long quantity, CreditSource source, String idempotencyKey);
    void requireAvailable(Long memberId, long quantity);
    CreditWalletResponse chargeSession(Long memberId, Long requestId, Long sessionId, long quantity);
    CreditReservationResponse reserve(Long memberId, Long requestId, long quantity);
    CreditReservationResponse capture(Long memberId, Long requestId, Long sessionId);
    CreditReservationResponse release(Long memberId, Long requestId, String reason);
    CreditReservationResponse inspectReservation(Long memberId, Long requestId);
}
