package fit.iuh.se.hsapplication.controller.doctor;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.dto.DoctorAvailabilityDto;
import fit.iuh.se.hschat.dto.request.UpdateDoctorAvailabilityRequest;
import fit.iuh.se.hschat.service.doctor.DoctorCareProfileService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class DoctorCareProfileControllerTest {

    DoctorCareProfileService service = mock(DoctorCareProfileService.class);
    DoctorCareProfileController controller = new DoctorCareProfileController(service);

    @Test
    void doctorSelfServiceUsesAuthenticatedDoctorId() {
        UserAuthentication doctor = UserAuthentication.builder().userId(10L).role(UserRole.DOCTOR).build();
        UpdateDoctorAvailabilityRequest request = new UpdateDoctorAvailabilityRequest(
                DoctorAvailabilityDto.builder().weekly(List.of()).build(),
                "Asia/Ho_Chi_Minh");

        controller.getOwnProfile(doctor);
        controller.updateOwnAvailability(doctor, request);

        verify(service).getOwnProfile(10L);
        verify(service).updateOwnAvailability(10L, request);
    }

    @Test
    void nonDoctorCannotUseDoctorCareProfileEndpoints() {
        UserAuthentication member = UserAuthentication.builder().userId(20L).role(UserRole.MEMBER).build();
        UpdateDoctorAvailabilityRequest request = new UpdateDoctorAvailabilityRequest(
                DoctorAvailabilityDto.builder().weekly(List.of()).build(),
                "Asia/Ho_Chi_Minh");

        assertThrows(AppException.class, () -> controller.getOwnProfile(member));
        assertThrows(AppException.class, () -> controller.updateOwnAvailability(member, request));

        verifyNoInteractions(service);
    }
}
