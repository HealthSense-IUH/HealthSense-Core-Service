package fit.iuh.se.hsapplication.config.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class RedisInitConfig implements ApplicationRunner {

    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        log.info("Verifying active connection to Redis server on startup...");
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            String pingResult = connection.ping();
            log.info("Successfully connected to Redis server. PING -> {}", pingResult);
        } catch (Exception e) {
            log.error("CRITICAL: Failed to connect to Redis server: {}", e.getMessage());
            throw new IllegalStateException("Failed to connect to Redis server on startup: " + e.getMessage(), e);
        }
    }
}
