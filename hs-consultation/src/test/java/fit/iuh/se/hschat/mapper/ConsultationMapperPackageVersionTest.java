package fit.iuh.se.hschat.mapper;

import fit.iuh.se.hschat.dto.response.CareServicePackageResponse;
import fit.iuh.se.hschat.entity.CareServicePackage;
import fit.iuh.se.hschat.entity.enums.CareServiceCode;
import fit.iuh.se.hschat.entity.enums.CareServicePackageStatus;
import fit.iuh.se.hschat.entity.enums.CareServiceSupportPolicy;
import fit.iuh.se.hschat.entity.enums.DoctorSpecialty;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsultationMapperPackageVersionTest {

    ConsultationMapper mapper = Mappers.getMapper(ConsultationMapper.class);

    @Test
    void packageVersionFieldsSerializeToResponse() {
        CareServicePackage version = CareServicePackage.builder()
                .id(11L)
                .familyId(10L)
                .code("CARDIO")
                .versionNumber(2)
                .name("Cardio care")
                .shortDescription("Short")
                .description("Detailed")
                .priceAmount(new BigDecimal("1290000.00"))
                .currency("VND")
                .durationDays(14)
                .includedServices(List.of(CareServiceCode.HEALTH_RECORD_REVIEW))
                .excludedServices(List.of(CareServiceCode.EMERGENCY_CARE))
                .requiredSpecialty(DoctorSpecialty.CARDIOLOGY)
                .supportPolicy(CareServiceSupportPolicy.ASSIGNED_DOCTOR_SUPPORT_SCHEDULE)
                .renewable(true)
                .termsPolicyReference("terms:v2")
                .status(CareServicePackageStatus.ACTIVE)
                .build();

        CareServicePackageResponse response = mapper.toCareServicePackageResponse(version);

        assertEquals(10L, response.getFamilyId());
        assertEquals(2, response.getVersion());
        assertEquals("Detailed", response.getDescription());
        assertEquals("Detailed", response.getDetailedDescription());
        assertEquals(List.of(CareServiceCode.HEALTH_RECORD_REVIEW), response.getIncludedServices());
        assertEquals(List.of(CareServiceCode.EMERGENCY_CARE), response.getExcludedServices());
        assertEquals(DoctorSpecialty.CARDIOLOGY, response.getRequiredSpecialty());
        assertEquals(
                CareServiceSupportPolicy.ASSIGNED_DOCTOR_SUPPORT_SCHEDULE,
                response.getSupportPolicy()
        );
        assertEquals("terms:v2", response.getTermsPolicyReference());
        assertEquals(true, response.getRenewable());
    }
}
