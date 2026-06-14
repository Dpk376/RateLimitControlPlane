package com.example.gateway.core.ratelimit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;

@Service
public class RedisRateLimiterService {

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<Long> rateLimitScript;

    public RedisRateLimiterService(StringRedisTemplate redisTemplate, RedisScript<Long> rateLimitScript) {
        this.redisTemplate = redisTemplate;
        this.rateLimitScript = rateLimitScript;
    }

    public boolean isAllowed(String tenantId, int capacity, int replenishRate) {
        String key = "rate_limit:" + tenantId;
        long now = Instant.now().getEpochSecond();
        
        try {
            Long result = redisTemplate.execute(
                    rateLimitScript,
                    Collections.singletonList(key),
                    String.valueOf(capacity),
                    String.valueOf(replenishRate),
                    String.valueOf(now)
            );
            return result != null && result == 1L;
        } catch (Exception e) {
            // Fallback in case Redis is down. In a production environment, we might want a circuit breaker here.
            // For now, allow the request but log the error.
            return true;
        }
    }
}
