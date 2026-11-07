package com.luminacare.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;

    @Override
    public Health health() {
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            String pingResponse = connection.ping();
            if ("PONG".equalsIgnoreCase(pingResponse)) {
                return Health.up().withDetail("status", "Available").build();
            }
            return Health.down().withDetail("error", "Unexpected ping response: " + pingResponse).build();
        } catch (Exception e) {
            log.error("Redis health check failed", e);
            return Health.down().withDetail("error", e.getMessage()).build();
        }
    }
}
