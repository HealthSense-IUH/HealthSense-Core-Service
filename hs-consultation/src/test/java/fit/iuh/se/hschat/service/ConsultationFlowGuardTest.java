package fit.iuh.se.hschat.service;

import fit.iuh.se.hschat.entity.ConsultationRequest;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.DoctorCareProfile;
import fit.iuh.se.hschat.entity.enums.ConsultationFlowType;
import fit.iuh.se.hschat.entity.enums.DoctorDispatchStatus;
import fit.iuh.se.hsshared.advice.entity.AppException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConsultationFlowGuardTest {

    @Test
    void buildersKeepCurrentProductionObjectsInLegacyFlow() {
        ConsultationRequest request = ConsultationRequest.builder().build();
        ConsultationSession session = ConsultationSession.builder().build();

        assertEquals(ConsultationFlowType.LEGACY_V3, request.getFlowType());
        assertEquals(ConsultationFlowType.LEGACY_V3, session.getFlowType());
        assertDoesNotThrow(() -> ConsultationFlowGuard.requireLegacy(request));
        assertDoesNotThrow(() -> ConsultationFlowGuard.requireLegacy(session));
    }

    @Test
    void queueObjectsCannotEnterLegacyCommercialWorkflow() {
        ConsultationRequest request = ConsultationRequest.builder()
                .flowType(ConsultationFlowType.QUEUE_DISPATCH_V1)
                .build();
        ConsultationSession session = ConsultationSession.builder()
                .flowType(ConsultationFlowType.QUEUE_DISPATCH_V1)
                .build();

        assertThrows(AppException.class, () -> ConsultationFlowGuard.requireLegacy(request));
        assertThrows(AppException.class, () -> ConsultationFlowGuard.requireLegacy(session));
    }

    @Test
    void newDoctorProfileDefaultsToUnavailableWithoutStopRequest() {
        DoctorCareProfile profile = DoctorCareProfile.builder().build();

        assertEquals(DoctorDispatchStatus.UNAVAILABLE, profile.getDispatchStatus());
        assertFalse(profile.getStopAfterCurrentSession());
        assertNull(profile.getBusySessionId());
        assertNotNull(profile.getDispatchStatusChangedAt());
    }
}
