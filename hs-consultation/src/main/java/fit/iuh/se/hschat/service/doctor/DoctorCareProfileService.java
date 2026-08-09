package fit.iuh.se.hschat.service.doctor;

import fit.iuh.se.hschat.dto.request.DoctorCareProfileRequest;
import fit.iuh.se.hschat.dto.response.DoctorCareProfileResponse;
import fit.iuh.se.hsuser.entity.enums.UserRole;

public interface DoctorCareProfileService {

    DoctorCareProfileResponse getProfile(Long actorId, UserRole actorRole, Long doctorId);

    DoctorCareProfileResponse upsertProfile(Long actorId, UserRole actorRole, Long doctorId, DoctorCareProfileRequest request);
}
