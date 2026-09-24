package com.jobflow.api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class RateLimiter {

    @Value("${jobflow.rate-limit.capacity:20}")
    private int capacity;

    @Value("${jobflow.rate-limit.refill-per-second:1}")
    private double refillPerSecond;

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean tryConsume(String apiKey) {
        Bucket bucket = buckets.computeIfAbsent(apiKey, k -> new Bucket(capacity));
        return bucket.tryConsume(refillPerSecond, capacity);
    }

    private static class Bucket {
        private double tokens;
        private long lastRefillNanos;
        private final ReentrantLock lock = new ReentrantLock();

        Bucket(int initialTokens) {
            this.tokens = initialTokens;
            this.lastRefillNanos = System.nanoTime();
        }

        boolean tryConsume(double refillPerSecond, int capacity) {
            lock.lock();
            try {
                long now = System.nanoTime();
                double elapsedSeconds = (now - lastRefillNanos) / 1_000_000_000.0;
                tokens = Math.min(capacity, tokens + elapsedSeconds * refillPerSecond);
                lastRefillNanos = now;

                if (tokens >= 1.0) {
                    tokens -= 1.0;
                    return true;
                }
                return false;
            } finally {
                lock.unlock();
            }
        }
    }
}