package fit.iuh.se.hschat.service;

import fit.iuh.se.hschat.entity.ConsultationRequest;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.enums.ConsultationFlowType;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsshared.advice.entity.enums.ErrorCode;

public final class ConsultationFlowGuard {

    private ConsultationFlowGuard() {
    }

    public static void requireLegacy(ConsultationRequest request) {
        if (request.getFlowType() != ConsultationFlowType.LEGACY_V3)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS,
                    "Queue Dispatch requests cannot use the legacy commercial workflow");
    }

    public static void requireLegacy(ConsultationSession session) {
        if (session.getFlowType() != ConsultationFlowType.LEGACY_V3)
            throw new AppException(ErrorCode.INVALID_CONSULTATION_STATUS,
                    "Queue Dispatch sessions cannot use the legacy commercial workflow");
    }
}
