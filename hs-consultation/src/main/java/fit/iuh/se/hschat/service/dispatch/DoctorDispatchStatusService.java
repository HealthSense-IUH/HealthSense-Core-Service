package fit.iuh.se.hschat.service.dispatch;

import fit.iuh.se.hschat.dto.request.UpdateDoctorDispatchPreferencesRequest;
import fit.iuh.se.hschat.dto.request.UpdateDoctorDispatchStatusRequest;
import fit.iuh.se.hschat.dto.response.DoctorDispatchStatusResponse;

public interface DoctorDispatchStatusService {

    DoctorDispatchStatusResponse getStatus(Long doctorId);

    DoctorDispatchStatusResponse updateStatus(Long doctorId, UpdateDoctorDispatchStatusRequest request);

    DoctorDispatchStatusResponse updatePreferences(Long doctorId, UpdateDoctorDispatchPreferencesRequest request);
}
