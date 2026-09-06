package fit.iuh.se.hsapplication.controller.doctor;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.request.UpdateDoctorDispatchPreferencesRequest;
import fit.iuh.se.hschat.dto.request.UpdateDoctorDispatchStatusRequest;
import fit.iuh.se.hschat.dto.response.DoctorDispatchStatusResponse;
import fit.iuh.se.hschat.entity.enums.DoctorDispatchStatus;
import fit.iuh.se.hschat.service.dispatch.DoctorDispatchStatusService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DoctorDispatchControllerTest {

    DoctorDispatchStatusService service = mock(DoctorDispatchStatusService.class);
    DoctorDispatchController controller = new DoctorDispatchController(service);

    @Test
    void doctorSelfServiceAlwaysUsesAuthenticatedDoctorId() {
        UserAuthentication doctor = UserAuthentication.builder().userId(10L).role(UserRole.DOCTOR).build();
        UpdateDoctorDispatchStatusRequest request =
                new UpdateDoctorDispatchStatusRequest(DoctorDispatchStatus.AVAILABLE);
        when(service.updateStatus(10L, request)).thenReturn(response());

        controller.updateStatus(doctor, request);

        verify(service).updateStatus(10L, request);
    }

    @Test
    void nonDoctorCannotMutateDispatchStatusOrPreferences() {
        UserAuthentication member = UserAuthentication.builder().userId(20L).role(UserRole.MEMBER).build();

        assertThrows(AppException.class, () -> controller.updateStatus(member,
                new UpdateDoctorDispatchStatusRequest(DoctorDispatchStatus.AVAILABLE)));
        assertThrows(AppException.class, () -> controller.updatePreferences(member,
                new UpdateDoctorDispatchPreferencesRequest(true)));

        verifyNoInteractions(service);
    }

    @Test
    void unauthenticatedCallerCannotReadDoctorStatus() {
        assertThrows(AppException.class, () -> controller.getStatus(null));
        verifyNoInteractions(service);
    }

    private DoctorDispatchStatusResponse response() {
        return new DoctorDispatchStatusResponse(
                10L, DoctorDispatchStatus.AVAILABLE, true, false, null, null);
    }
}
