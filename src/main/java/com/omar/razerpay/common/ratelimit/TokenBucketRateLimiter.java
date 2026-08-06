package com.omar.razerpay.common.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rate-limit.method", havingValue = "token-bucket")
public class TokenBucketRateLimiter implements RateLimiter {

    private final StringRedisTemplate redisTemplate;

    /*
     * LUA SCRIPT EXPLANATION (Lazy Refill):
     *
     * KEYS[1] = User's bucket key
     * ARGV[1] = Bucket Capacity (Max Tokens)
     * ARGV[2] = Refill Amount (Tokens to add)
     * ARGV[3] = Refill Period (Seconds)
     * ARGV[4] = Current Timestamp (Seconds)
     *
     * Returns: { 1 (Allowed) or 0 (Denied), Retry-After seconds, Remaining Tokens }
     */
    private static final String LUA_SCRIPT = """
        local key = KEYS[1]
        local capacity = tonumber(ARGV[1])
        local refill_tokens = tonumber(ARGV[2])
        local refill_seconds = tonumber(ARGV[3])
        local now = tonumber(ARGV[4])
        
        -- Calculate TTL: How long it takes to fully refill the bucket, times 2 for safety
        local ttl = math.ceil((capacity / refill_tokens) * refill_seconds * 2)
        
        -- Get current bucket state
        local bucket = redis.call('HMGET', key, 'tokens', 'last_refill')
        local last_tokens = tonumber(bucket[1])
        local last_refill = tonumber(bucket[2])
        
        local current_tokens = capacity
        
        -- If the bucket exists, apply Lazy Refill
        if last_tokens ~= nil and last_refill ~= nil then
            local elapsed_seconds = math.max(0, now - last_refill)
            local tokens_to_add = math.floor((elapsed_seconds / refill_seconds) * refill_tokens)
            
            -- Important: To prevent fractional token starvation, we only advance the 
            -- timestamp by the exact time it took to generate the refilled tokens.
            if tokens_to_add > 0 then
                last_refill = last_refill + ((tokens_to_add / refill_tokens) * refill_seconds)
                current_tokens = math.min(capacity, last_tokens + tokens_to_add)
            else
                current_tokens = last_tokens
            end
        else
            -- First time seeing this user, start with a full bucket
            last_refill = now
        end
        
        -- Check if request is allowed
        if current_tokens >= 1 then
            current_tokens = current_tokens - 1
            redis.call('HMSET', key, 'tokens', current_tokens, 'last_refill', last_refill)
            redis.call('EXPIRE', key, ttl)
            return {1, 0, current_tokens}
        else
            -- Denied: Calculate how long until exactly 1 token is refilled
            local wait_time = math.ceil(refill_seconds / refill_tokens)
            return {0, wait_time, current_tokens}
        end
        """;

    private final DefaultRedisScript<List> script = new DefaultRedisScript<>(LUA_SCRIPT, List.class);

    // Configuration: Max 10 tokens, refills 2 tokens every 10 seconds.
    // This allows a burst of 10 requests, but a sustained rate of 12 requests per minute.
    private static final int REFILL_TOKENS = 2;
    private static final int REFILL_PERIOD_SECONDS = 10;

    @Override
    public RateLimitResult check(String key, int maxRequestsAllowed, long windowSeconds) {
        try {
            long nowSeconds = Instant.now().getEpochSecond();
            String redisKey = "rateLimit:token_bucket:" + key;

            List<Long> result = redisTemplate.execute(
                    script,
                    List.of(redisKey),
                    String.valueOf(maxRequestsAllowed),
                    String.valueOf(REFILL_TOKENS),
                    String.valueOf(REFILL_PERIOD_SECONDS),
                    String.valueOf(nowSeconds)
            );

            if (result == null || result.size() != 3) {
                log.error("Unexpected Lua script result for key: {}", key);
                return RateLimitResult.allowed(maxRequestsAllowed); // Fail-open
            }

            boolean isAllowed = result.get(0) == 1L;
            int retryAfter = result.get(1).intValue();
            int remainingTokens = result.get(2).intValue();

            if (!isAllowed) {
                log.warn("Token Bucket rate limit exceeded for user: {}. Retry in {}s", key, retryAfter);
                return RateLimitResult.denied(retryAfter);
            }

            return RateLimitResult.allowed(remainingTokens);

        } catch (Exception ex) {
            log.error("Redis Token Bucket rate limiting failed for key {}: {}", key, ex.getMessage());
            return RateLimitResult.allowed(maxRequestsAllowed); // Fail-open
        }
    }
}