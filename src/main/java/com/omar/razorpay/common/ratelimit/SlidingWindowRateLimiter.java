package com.omar.razorpay.common.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.rate-limit.method", havingValue = "sliding")
public class SlidingWindowRateLimiter implements RateLimiter {

    private final StringRedisTemplate redis;

    @Override
    public RateLimitResult check(String key, int maxRequestsAllowed, long windowSeconds) {
        long nowMs = System.currentTimeMillis();
        long floorMs = nowMs - windowSeconds*1000;

        String redisKey = "rateLimit:sliding" + key;

        var zset = redis.opsForZSet();
        zset.removeRangeByScore(redisKey, Double.NEGATIVE_INFINITY, floorMs);
        Long count = zset.zCard(redisKey);
        long current = count != null ? count : 0;

        if(current >= maxRequestsAllowed){
            var oldestRequest  = zset.rangeWithScores(redisKey, 0, 0);
            int retryAfter = 1;

            if(oldestRequest != null && !oldestRequest.isEmpty()){
                Double oldestScore = oldestRequest.iterator().next().getScore();
                if(oldestScore != null)
                    retryAfter = (int) Math.ceil((oldestScore.longValue() + windowSeconds*1000 - nowMs)/1000.0);
            }

            return RateLimitResult.denied(retryAfter);
        }

        zset.add(redisKey, UUID.randomUUID().toString(), nowMs);
        redis.expire(redisKey, java.time.Duration.ofSeconds(windowSeconds+1));
        return RateLimitResult.allowed(maxRequestsAllowed - (int) current - 1);
    }
}
