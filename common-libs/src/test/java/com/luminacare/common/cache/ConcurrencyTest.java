package com.luminacare.common.cache;

import com.luminacare.common.config.RedisConfig;
import com.luminacare.common.constants.Headers;
import com.luminacare.common.context.TenantContext;
import com.luminacare.common.util.CacheKeyBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest(classes = {CacheKeyBuilder.class, RedisConfig.class})
class ConcurrencyTest {

    @Container
    public static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        registry.add("spring.application.name", () -> "concurrencytest");
    }

    @Autowired
    private CacheKeyBuilder cacheKeyBuilder;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void testConcurrentMultiTenantCacheAccess() throws InterruptedException {
        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger errorCount = new AtomicInteger(0);

        String[] orgs = {"orgA", "orgB", "orgC"};

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    String org = orgs[index % 3];
                    MockHttpServletRequest request = new MockHttpServletRequest();
                    request.addHeader(Headers.X_ORG_ID, org);
                    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
                    TenantContext.setTenantId(org);

                    String key = cacheKeyBuilder.build(1L); // Same ID for everyone
                    redisTemplate.opsForValue().set(key, "Data-" + org);

                    // Ensure I get my own data back, no bleed
                    String retrieved = (String) redisTemplate.opsForValue().get(key);
                    if (!("Data-" + org).equals(retrieved)) {
                        errorCount.incrementAndGet();
                    }

                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    RequestContextHolder.resetRequestAttributes();
                    TenantContext.clear();
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        assertEquals(0, errorCount.get(), "Concurrency issues detected! Cache data leaked across threads/tenants.");
    }
}
