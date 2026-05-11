package com.wonwire.wonwire.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for Redis operations.
 * Used for JWT blacklisting and rate limiting.
 */
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Stores a key-value pair in Redis with an expiration time.
     */
    public void set(String key, String value, long duration, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, duration, unit);
    }

    /**
     * Retrieves a value from Redis by key.
     * Returns null if the key doesn't exist or has expired.
     */
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Checks if a key exists in Redis.
     */
    public boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * Increments a counter in Redis and returns the new value.
     * Used for rate limiting.
     */
    public long increment(String key) {
        Long value = redisTemplate.opsForValue().increment(key);
        return value != null ? value : 0L;
    }

    /**
     * Sets expiration on an existing key.
     */
    public void expire(String key, long duration, TimeUnit unit) {
        redisTemplate.expire(key, duration, unit);
    }
}