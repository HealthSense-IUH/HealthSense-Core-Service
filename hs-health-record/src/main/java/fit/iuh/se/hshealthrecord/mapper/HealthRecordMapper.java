package fit.iuh.se.hshealthrecord.mapper;

import fit.iuh.se.hshealthrecord.dto.response.HealthRecordResponse;
import fit.iuh.se.hshealthrecord.entity.HealthRecord;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HealthRecordMapper {

    HealthRecordResponse toResponse(HealthRecord healthRecord);
}
