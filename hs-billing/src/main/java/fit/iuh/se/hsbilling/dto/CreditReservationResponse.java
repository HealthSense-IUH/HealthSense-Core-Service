package fit.iuh.se.hsbilling.dto;

import fit.iuh.se.hsbilling.entity.enums.CreditReservationStatus;

public record CreditReservationResponse(Long requestId, Long sessionId, long quantity,
        CreditReservationStatus status, CreditWalletResponse wallet) {}
