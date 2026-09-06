package fit.iuh.se.hsapplication.controller.doctor;

import fit.iuh.se.hsapplication.dto.auth.UserAuthentication;
import fit.iuh.se.hschat.service.dispatch.DoctorOfferService;
import fit.iuh.se.hsshared.advice.entity.AppException;
import fit.iuh.se.hsuser.entity.enums.UserRole;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class DoctorConsultationOfferControllerTest {
    DoctorOfferService service = mock(DoctorOfferService.class);
    DoctorConsultationOfferController controller = new DoctorConsultationOfferController(service);
    UserAuthentication doctor = UserAuthentication.builder().userId(10L).role(UserRole.DOCTOR).build();

    @Test void doctorCanOnlyOperateAsAuthenticatedIdentity() {
        controller.current(doctor);
        controller.accept(doctor, "offer-a");
        controller.reject(doctor, "offer-b");
        verify(service).getCurrentOffer(10L);
        verify(service).accept(10L, "offer-a");
        verify(service).reject(10L, "offer-b");
    }

    @Test void memberCannotReadAcceptOrRejectDoctorOffer() {
        UserAuthentication member = UserAuthentication.builder().userId(20L).role(UserRole.MEMBER).build();
        assertThrows(AppException.class, () -> controller.current(member));
        assertThrows(AppException.class, () -> controller.accept(member, "offer-a"));
        assertThrows(AppException.class, () -> controller.reject(member, "offer-a"));
        verifyNoInteractions(service);
    }

    @Test void unauthenticatedCallerIsRejected() {
        assertThrows(AppException.class, () -> controller.current(null));
        verifyNoInteractions(service);
    }
}
