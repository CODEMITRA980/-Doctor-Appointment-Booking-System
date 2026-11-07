package com.luminacare.common.cache;

import com.luminacare.common.config.RedisConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest(classes = {CacheIntegrationTest.TestConfig.class, RedisConfig.class})
class CacheIntegrationTest {

    @Container
    public static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        registry.add("spring.application.name", () -> "testapp");
    }

    @Autowired
    private DummyService dummyService;

    @Test
    void testCacheHitMissAndUpdateAndEvict() {
        // 1. Initial Call -> MISS -> hits DB (Atomic counter increments)
        String val1 = dummyService.getData("123");
        assertEquals("Data-123", val1);
        assertEquals(1, dummyService.getInvocationCount());

        // 2. Second Call -> HIT -> skips DB
        String val2 = dummyService.getData("123");
        assertEquals("Data-123", val2);
        assertEquals(1, dummyService.getInvocationCount()); // count remains 1

        // 3. Update -> Refreshes Cache (CachePut)
        dummyService.updateData("123", "NewData-123");
        
        // 4. Third Call -> HIT on new data -> skips DB
        String val3 = dummyService.getData("123");
        assertEquals("NewData-123", val3);
        assertEquals(1, dummyService.getInvocationCount()); // still 1

        // 5. Evict Cache
        dummyService.deleteData("123");

        // 6. Fourth Call -> MISS -> hits DB
        dummyService.getData("123");
        assertEquals(2, dummyService.getInvocationCount()); // count is now 2
    }

    @Configuration
    @EnableAutoConfiguration
    static class TestConfig {
        @Bean
        public DummyService dummyService() {
            return new DummyService();
        }
    }

    @Service
    static class DummyService {
        private final AtomicInteger invocationCount = new AtomicInteger(0);

        @Cacheable(cacheNames = "testCache", key = "#id")
        public String getData(String id) {
            invocationCount.incrementAndGet();
            return "Data-" + id;
        }

        @CachePut(cacheNames = "testCache", key = "#id")
        public String updateData(String id, String newData) {
            return newData;
        }

        @CacheEvict(cacheNames = "testCache", key = "#id")
        public void deleteData(String id) {
            // delete logic
        }

        public int getInvocationCount() {
            return invocationCount.get();
        }
    }
}
