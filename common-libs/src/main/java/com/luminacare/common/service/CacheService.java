package com.luminacare.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void put(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            log.warn("Failed to put key '{}' in cache", key, e);
        }
    }

    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (type.isInstance(value)) {
                return Optional.of(type.cast(value));
            }
        } catch (Exception e) {
            log.warn("Failed to get key '{}' from cache", key, e);
        }
        return Optional.empty();
    }

    public void evict(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Failed to evict key '{}' from cache", key, e);
        }
    }

    /**
     * Attempts to acquire a lock to prevent cache stampede.
     * @param key Lock key
     * @param ttl Duration for the lock
     * @return true if lock acquired successfully
     */
    public boolean tryLock(String key, Duration ttl) {
        try {
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, "LOCKED", ttl);
            return Boolean.TRUE.equals(acquired);
        } catch (Exception e) {
            log.warn("Failed to acquire lock for key '{}'", key, e);
            return false;
        }
    }

    public void releaseLock(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Failed to release lock for key '{}'", key, e);
        }
    }
}
