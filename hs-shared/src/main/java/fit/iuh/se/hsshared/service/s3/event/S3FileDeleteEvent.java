package fit.iuh.se.hsshared.service.s3.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class S3FileDeleteEvent {
    private String objectKey;
}
