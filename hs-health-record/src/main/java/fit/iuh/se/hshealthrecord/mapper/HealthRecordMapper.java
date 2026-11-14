package fit.iuh.se.hshealthrecord.mapper;

import fit.iuh.se.hshealthrecord.dto.response.HealthRecordResponse;
import fit.iuh.se.hshealthrecord.entity.HealthRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface HealthRecordMapper {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    TypeReference<LinkedHashMap<String, Object>> HRV_FEATURES_TYPE = new TypeReference<>() {};

    @Mapping(target = "hrvFeatures", source = "hrvFeaturesJson", qualifiedByName = "toHrvFeatures")
    HealthRecordResponse toResponse(HealthRecord healthRecord);

    @Mapping(target = "hrvFeatures", source = "hrvFeaturesJson", qualifiedByName = "toListHrvFeatures")
    HealthRecordResponse toListResponse(HealthRecord healthRecord);

    @Named("toHrvFeatures")
    default Map<String, Object> toHrvFeatures(String hrvFeaturesJson) {
        if (hrvFeaturesJson == null || hrvFeaturesJson.trim().isEmpty())
            return null;
        try {
            return OBJECT_MAPPER.readValue(hrvFeaturesJson, HRV_FEATURES_TYPE);
        } catch (Exception exception) {
            return null;
        }
    }

    @Named("toListHrvFeatures")
    default Map<String, Object> toListHrvFeatures(String hrvFeaturesJson) {
        Map<String, Object> features = toHrvFeatures(hrvFeaturesJson);
        if (features != null) {
            features.remove("chartData");
            features.remove("windowAnalysis");
            features.remove("nnIntervals");
        }
        return features;
    }
}
