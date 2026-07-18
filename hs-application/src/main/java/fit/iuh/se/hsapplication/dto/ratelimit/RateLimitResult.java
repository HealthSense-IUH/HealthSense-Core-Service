package fit.iuh.se.hsapplication.dto.ratelimit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Builder
public class RateLimitResult{
    boolean allowed;
    long limit;
    long remaining;
    long retryAfterSeconds;
}
