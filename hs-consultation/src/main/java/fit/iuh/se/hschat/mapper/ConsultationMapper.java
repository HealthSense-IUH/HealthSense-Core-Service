package fit.iuh.se.hschat.mapper;

import fit.iuh.se.hschat.dto.response.ConsultationMessageResponse;
import fit.iuh.se.hschat.dto.response.ConsultationParticipantResponse;
import fit.iuh.se.hschat.dto.response.ConsultationRequestResponse;
import fit.iuh.se.hschat.dto.response.ConsultationSessionResponse;
import fit.iuh.se.hschat.dto.response.CareServicePackageResponse;
import fit.iuh.se.hschat.dto.response.DoctorCareProfileResponse;
import fit.iuh.se.hschat.dto.DoctorAvailabilityDto;
import fit.iuh.se.hschat.entity.CareServicePackage;
import fit.iuh.se.hschat.entity.ConsultationMessage;
import fit.iuh.se.hschat.entity.ConsultationParticipant;
import fit.iuh.se.hschat.entity.ConsultationRequest;
import fit.iuh.se.hschat.entity.ConsultationSession;
import fit.iuh.se.hschat.entity.DoctorCareProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.bson.Document;

import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring")
public interface ConsultationMapper {

    @Mapping(target = "unreadCount", ignore = true)
    @Mapping(target = "memberDisplayName", ignore = true)
    @Mapping(target = "doctorDisplayName", ignore = true)
    ConsultationSessionResponse toSessionResponse(ConsultationSession session);

    @Mapping(target = "selectedHealthRecords", ignore = true)
    @Mapping(target = "moreInfoHistory", ignore = true)
    ConsultationRequestResponse toRequestResponse(ConsultationRequest request);

    @Mapping(target = "version", source = "versionNumber")
    @Mapping(target = "detailedDescription", source = "description")
    CareServicePackageResponse toCareServicePackageResponse(CareServicePackage careServicePackage);

    default DoctorCareProfileResponse toDoctorCareProfileResponse(DoctorCareProfile doctorCareProfile) {
        if (doctorCareProfile == null)
            return null;

        return DoctorCareProfileResponse.builder()
                .id(doctorCareProfile.getId())
                .doctorId(doctorCareProfile.getDoctorId())
                .specialty(doctorCareProfile.getSpecialty())
                .acceptsOneOnOneCare(doctorCareProfile.getAcceptsOneOnOneCare())
                .maxActiveConsultations(doctorCareProfile.getMaxActiveConsultations())
                .availabilityJson(doctorCareProfile.getAvailabilityJson())
                .availability(toDoctorAvailability(doctorCareProfile.getAvailabilityJson()))
                .timezone(doctorCareProfile.getTimezone())
                .createdAt(doctorCareProfile.getCreatedAt())
                .updatedAt(doctorCareProfile.getUpdatedAt())
                .build();
    }

    ConsultationMessageResponse toMessageResponse(ConsultationMessage message);

    ConsultationParticipantResponse toParticipantResponse(ConsultationParticipant participant);

    default DoctorAvailabilityDto toDoctorAvailability(String availabilityJson) {
        if (availabilityJson == null || availabilityJson.trim().isEmpty())
            return null;
        try {
            Document root = Document.parse(availabilityJson);
            List<Document> weeklyDocuments = root.getList("weekly", Document.class);
            List<DoctorAvailabilityDto.WeeklySlot> weekly = weeklyDocuments == null
                    ? null
                    : weeklyDocuments.stream()
                    .filter(Objects::nonNull)
                    .map(document -> DoctorAvailabilityDto.WeeklySlot.builder()
                            .dayOfWeek(document.getString("dayOfWeek"))
                            .start(document.getString("start"))
                            .end(document.getString("end"))
                            .build())
                    .toList();

            return DoctorAvailabilityDto.builder()
                    .weekly(weekly)
                    .build();
        } catch (Exception exception) {
            return null;
        }
    }
}
