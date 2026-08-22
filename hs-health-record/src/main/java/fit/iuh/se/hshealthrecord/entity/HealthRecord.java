package fit.iuh.se.hshealthrecord.entity;

import fit.iuh.se.hshealthrecord.entity.enums.PredictionLabel;
import fit.iuh.se.hshealthrecord.entity.enums.RecordStatus;
import fit.iuh.se.hsshared.generator.SnowflakeGenerated;
import fit.iuh.se.hsuser.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Entity
@Table(name = "health_records", indexes = {
    @Index(name = "idx_health_record_user", columnList = "user_id"),
    @Index(name = "idx_health_record_user_status_date", columnList = "user_id, status, created_at")
})
public class HealthRecord extends BaseEntity {
    @Id
    @SnowflakeGenerated
    @Column(name = "id", nullable = false, updatable = false)
    Long id;

    @Column(name = "user_id", nullable = false)
    Long userId;

    @Column(name = "file_name", nullable = false, length = 255)
    String fileName;

    @Column(name = "s3_file_key", nullable = false, length = 500)
    String s3FileKey;

    @Column(name = "file_size")
    Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    RecordStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "prediction_label", length = 30)
    PredictionLabel predictionLabel;

    @Column(name = "confidence")
    Double confidence;

    @Column(name = "hrv_features_json", columnDefinition = "TEXT")
    String hrvFeaturesJson;

    @Column(name = "error_message", columnDefinition = "TEXT")
    String errorMessage;
}
