package com.omar.razorpay.common.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rate-limit.method", havingValue = "sliding-lua")
public class SlidingWindowRateLimiterLua implements RateLimiter {

    private final StringRedisTemplate redis;

    /*
     * LUA SCRIPT EXPLANATION:
     * This script performs the exact same logic as the Java code, but atomically.
     *
     * KEYS[1] = redisKey
     * ARGV[1] = nowMs
     * ARGV[2] = floorMs (nowMs - windowSeconds * 1000)
     * ARGV[3] = maxRequestsAllowed
     * ARGV[4] = unique requestId (UUID)
     * ARGV[5] = expire TTL (windowSeconds + 1)
     *
     * Returns an array: { allowed (1 or 0), retryAfter (seconds), currentCount }
     */
    private static final String SCRIPT = """
        local key = KEYS[1]
        local now = tonumber(ARGV[1])
        local floor = tonumber(ARGV[2])
        local limit = tonumber(ARGV[3])
        local req_id = ARGV[4]
        local ttl = tonumber(ARGV[5])
        
        -- 1. Clean up old requests
        redis.call('ZREMRANGEBYSCORE', key, '-inf', floor)
        
        -- 2. Count current requests
        local current = redis.call('ZCARD', key)
        
        -- 3. Check limit
        if current >= limit then
            -- Denied: Calculate retryAfter based on the oldest score
            local oldest_req = redis.call('ZRANGE', key, 0, 0, 'WITHSCORES')
            local retry_after = 1 -- default fallback
        
            if oldest_req and oldest_req[2] then
                local oldest_score = tonumber(oldest_req[2])
                -- Formula: math.ceil(((oldest_score + window_ms) - now) / 1000)
                local window_ms = ttl * 1000 - 1000
                retry_after = math.ceil(((oldest_score + window_ms) - now) / 1000)
                if retry_after < 1 then retry_after = 1 end
            end
        
            return {0, retry_after, current}
        else
            -- Allowed: Add new request and update TTL
            redis.call('ZADD', key, now, req_id)
            redis.call('EXPIRE', key, ttl)
            return {1, 0, current}
        end
        """;

    private final DefaultRedisScript<List> script = new DefaultRedisScript<>(SCRIPT, List.class);

    @Override
    public RateLimitResult check(String key, int maxRequestsAllowed, long windowSeconds) {
        try {
            long nowMs = System.currentTimeMillis();
            long floorMs = nowMs - windowSeconds * 1000;
            String redisKey = "rateLimit:sliding:" + key;
            String reqId = UUID.randomUUID().toString();

            List<Long> result = redis.execute(
                    script,
                    List.of(redisKey),
                    String.valueOf(nowMs),
                    String.valueOf(floorMs),
                    String.valueOf(maxRequestsAllowed),
                    reqId,
                    String.valueOf(windowSeconds + 1)
            );

            if (result == null || result.size() != 3) {
                log.error("Unexpected Lua script result for key: {}", key);
                return RateLimitResult.allowed(maxRequestsAllowed); // Fail-open
            }

            boolean allowed = result.get(0) == 1L;
            int retryAfter = result.get(1).intValue();
            int currentCount = result.get(2).intValue();

            if (!allowed) {
                return RateLimitResult.denied(retryAfter);
            }

            return RateLimitResult.allowed(maxRequestsAllowed - currentCount - 1);

        } catch (Exception ex) {
            log.error("Redis sliding window rate limiting failed for key {}: {}", key, ex.getMessage());
            return RateLimitResult.allowed(maxRequestsAllowed); // Fail-open
        }
    }
}
