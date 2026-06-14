package com.example.gateway.core.ratelimit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.RedisScript;

@Configuration
public class RateLimiterConfig {

    @Bean
    public RedisScript<Long> rateLimitScript() {
        return RedisScript.of(new ClassPathResource("scripts/rate_limit.lua"), Long.class);
    }
}
