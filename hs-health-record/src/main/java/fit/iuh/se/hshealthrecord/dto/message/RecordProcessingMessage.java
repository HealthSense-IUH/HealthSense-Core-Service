package fit.iuh.se.hshealthrecord.dto.message;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecordProcessingMessage implements Serializable {
    Long recordId;
    String s3Key;
    Long userId;
    String fileName;
}
