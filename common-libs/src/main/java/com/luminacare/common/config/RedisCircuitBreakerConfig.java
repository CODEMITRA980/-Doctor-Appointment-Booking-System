package com.luminacare.common.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;

@Configuration
public class RedisCircuitBreakerConfig {

    public static final String REDIS_CB = "redis";

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(3)
                .slidingWindowSize(10) // open after 5 failures in a window of 10
                .minimumNumberOfCalls(5)
                .build();

        return CircuitBreakerRegistry.of(
                Map.of(REDIS_CB, circuitBreakerConfig)
        );
    }
}
