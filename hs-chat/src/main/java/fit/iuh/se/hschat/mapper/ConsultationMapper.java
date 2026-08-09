package fit.iuh.se.hschat.mapper;

import fit.iuh.se.hschat.dto.response.ConsultationMessageResponse;
import fit.iuh.se.hschat.dto.response.ConsultationParticipantResponse;
import fit.iuh.se.hschat.dto.response.ConsultationRequestResponse;
import fit.iuh.se.hschat.dto.response.ConsultationSessionResponse;
import fit.iuh.se.hschat.dto.response.CareServicePackageResponse;
import fit.iuh.se.hschat.dto.response.DoctorCareProfileResponse;
import fit.iuh.se.hschat.entity.CareServicePackage;
import fit.iuh.se.hschat.entity.ConsultationMessage;
import fit.iuh.se.hschat.entity.ConsultationParticipant;
import fit.iuh.se.hschat.entity.ConsultationRequest;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.DoctorCareProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConsultationMapper {

    @Mapping(target = "unreadCount", ignore = true)
    ConsultationSessionResponse toSessionResponse(ConsultationSession session);

    ConsultationRequestResponse toRequestResponse(ConsultationRequest request);

    CareServicePackageResponse toCareServicePackageResponse(CareServicePackage careServicePackage);

    DoctorCareProfileResponse toDoctorCareProfileResponse(DoctorCareProfile doctorCareProfile);

    ConsultationMessageResponse toMessageResponse(ConsultationMessage message);

    ConsultationParticipantResponse toParticipantResponse(ConsultationParticipant participant);
}
