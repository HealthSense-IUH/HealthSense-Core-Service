package fit.iuh.se.hsapplication.service.ratelimit;

import fit.iuh.se.hsapplication.dto.ratelimit.RateLimitResult;

public interface RateLimiterService {

    RateLimitResult consume(String key);
}
