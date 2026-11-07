package com.luminacare.common.resilience;

import com.luminacare.common.config.RedisCircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = RedisCircuitBreakerConfig.class)
class CircuitBreakerTest {

    @Autowired
    private CircuitBreakerRegistry registry;

    @Test
    void circuitBreakerShouldOpenAfterFailures() {
        CircuitBreaker cb = registry.circuitBreaker(RedisCircuitBreakerConfig.REDIS_CB);

        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());

        // Trigger failures to hit the 5 minimum calls and exceed the threshold
        for (int i = 0; i < 5; i++) {
            try {
                cb.executeRunnable(() -> {
                    throw new RuntimeException("Redis connection timeout");
                });
            } catch (Exception ignored) {
            }
        }

        assertEquals(CircuitBreaker.State.OPEN, cb.getState(), "Circuit breaker should OPEN after 5 consecutive failures");
    }
}
