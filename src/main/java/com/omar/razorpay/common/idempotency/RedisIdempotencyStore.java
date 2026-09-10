package com.omar.razorpay.common.idempotency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisIdempotencyStore implements IdempotencyStore {

    private final StringRedisTemplate redis;
    private final String PREFIX = "idempotency:";

    @Override
    public boolean setIfAbsent(String key, Duration ttl) {
        try{
            Boolean setNx = redis.opsForValue().setIfAbsent(PREFIX + key, IN_PROGRESS, ttl);
            return Boolean.TRUE.equals(setNx);
        }
        catch (Exception e){
            log.error("Error setting idempotency key in Redis", e);
            return true;
        }
    }

    @Override
    public void store(String key, String value, Duration ttl) {
        try{
            redis.opsForValue().set(PREFIX + key, value, ttl);
        }
        catch (Exception e){
            log.error("Error storing idempotency key in Redis", e);
        }
    }

    @Override
    public Optional<String> get(String key) {
        try{
            String value = redis.opsForValue().get(PREFIX + key);
            return Optional.ofNullable(value);
        }
        catch (Exception e){
            log.error("Error retrieving idempotency key from Redis", e);
            return Optional.empty();
        }
    }

    @Override
    public void delete(String key) {
        try{
            redis.delete(PREFIX + key);
        }
        catch (Exception e){
            log.error("Error deleting idempotency key from Redis", e);
        }
    }
}
