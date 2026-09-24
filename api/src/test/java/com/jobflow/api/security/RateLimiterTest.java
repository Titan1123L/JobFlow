package com.jobflow.api.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterTest {

    @Test
    void allowsRequestsUpToCapacity() {
        RateLimiter limiter = new RateLimiter();
        ReflectionTestUtils.setField(limiter, "capacity", 3);
        ReflectionTestUtils.setField(limiter, "refillPerSecond", 0.0); // no refill during this test

        assertThat(limiter.tryConsume("key-a")).isTrue();
        assertThat(limiter.tryConsume("key-a")).isTrue();
        assertThat(limiter.tryConsume("key-a")).isTrue();
    }

    @Test
    void rejectsRequestBeyondCapacity() {
        RateLimiter limiter = new RateLimiter();
        ReflectionTestUtils.setField(limiter, "capacity", 2);
        ReflectionTestUtils.setField(limiter, "refillPerSecond", 0.0);

        assertThat(limiter.tryConsume("key-b")).isTrue();
        assertThat(limiter.tryConsume("key-b")).isTrue();
        assertThat(limiter.tryConsume("key-b")).isFalse(); // bucket now empty
    }

    @Test
    void differentApiKeysHaveIndependentBuckets() {
        RateLimiter limiter = new RateLimiter();
        ReflectionTestUtils.setField(limiter, "capacity", 1);
        ReflectionTestUtils.setField(limiter, "refillPerSecond", 0.0);

        assertThat(limiter.tryConsume("key-c")).isTrue();
        assertThat(limiter.tryConsume("key-c")).isFalse(); // key-c exhausted

        // A completely different key should be unaffected
        assertThat(limiter.tryConsume("key-d")).isTrue();
    }

    @Test
    void refillsTokensOverTime() throws InterruptedException {
        RateLimiter limiter = new RateLimiter();
        ReflectionTestUtils.setField(limiter, "capacity", 1);
        ReflectionTestUtils.setField(limiter, "refillPerSecond", 10.0); // fast refill, for a quick test

        assertThat(limiter.tryConsume("key-e")).isTrue();
        assertThat(limiter.tryConsume("key-e")).isFalse(); // empty immediately after

        Thread.sleep(150); // 10 tokens/sec * 0.15s = 1.5 tokens refilled, capped at capacity=1

        assertThat(limiter.tryConsume("key-e")).isTrue(); // should have refilled by now
    }
}