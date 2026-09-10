package com.omar.razorpay.merchant.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisApiKeyCache implements ApiKeyCache {

    private static final String PREFIX = "apiKey:";
    private static final Duration TTL = Duration.ofMinutes(5);
 
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Optional<ApiKeyCacheEntry> get(String keyId) {
        try{
            String json = stringRedisTemplate.opsForValue().get(PREFIX + keyId);

            if(json == null || json.isEmpty()){
                return Optional.empty();
            }

            return Optional.of(objectMapper.readValue(json, ApiKeyCacheEntry.class));

        }
        catch (Exception ex){
            log.warn("Api key cache read failed for keyId: {} - Reason: {}", keyId, ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void put(String keyId, ApiKeyCacheEntry entry) {
        try{
            String json = objectMapper.writeValueAsString(entry);
            stringRedisTemplate.opsForValue().set(PREFIX + keyId, json, TTL);
        }
        catch (Exception ex){
            log.warn("Api key cache write failed for keyId: {} - Reason: {}", keyId, ex.getMessage());
        }
    }

    @Override
    public void evict(String keyId) {
        try{
            stringRedisTemplate.delete(PREFIX + keyId);
        }
        catch (Exception ex){
            log.warn("Api key cache eviction failed for keyId: {} - Reason: {}", keyId, ex.getMessage());
        }
    }
}
