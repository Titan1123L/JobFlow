package com.jobflow.worker.stats;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class StatsRecorder {

    private static final String COMPLETED_COUNT_KEY = "stats:completed:count";
    private static final String COMPLETED_DURATION_KEY = "stats:completed:totalDurationMs";

    private final StringRedisTemplate redisTemplate;

    public StatsRecorder(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void recordCompletion(long durationMs) {
        try {
            redisTemplate.opsForValue().increment(COMPLETED_COUNT_KEY);
            redisTemplate.opsForValue().increment(COMPLETED_DURATION_KEY, durationMs);
        } catch (Exception ex) {
            // Per ARCHITECTURE.md: Redis being down must never fail job processing itself
            System.out.println("Could not record stats to Redis (non-fatal): " + ex.getMessage());
        }
    }
}