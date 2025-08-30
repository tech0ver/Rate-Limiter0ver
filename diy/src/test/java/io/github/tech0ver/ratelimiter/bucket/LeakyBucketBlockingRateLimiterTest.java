package io.github.tech0ver.ratelimiter.bucket;

import io.github.tech0ver.ratelimiter.MockWatch;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LeakyBucketBlockingRateLimiterTest {

    /**
     * <pre>
     *     t q  +  -
     * 0.000 0 +1 -1
     *  .250 0 +2 -1
     *  .500 1 +2 -1
     *  .750 2 +1 -1
     * 1.000 2    -1
     *  .250 1    -1
     *  .500 0 +5 -1 >1
     *  .750 3 +2 -1 >1
     * 2.000 3 +1 -1
     *  .250 3    -1
     *  .500 2    -1
     *  .750 1    -1
     * 3.000 0
     * </pre>
     */
    @Test
    void should_shape_traffic() {
        int capacity = 3;
        Duration requestInterval = Duration.ofMillis(250);
        MockWatch watch = new MockWatch();
        var limiter = new LeakyBucketBlockingRateLimiter(capacity, requestInterval, watch);
        long nanosPerRequest = requestInterval.toNanos();
        // 0.000
        assertEquals(0L, limiter.getDelayNanos("r1"));
        watch.advance(nanosPerRequest);
        // 0.250
        assertEquals(0L, limiter.getDelayNanos("r1"));
        assertEquals(nanosPerRequest, limiter.getDelayNanos("r1"));
        watch.advance(nanosPerRequest);
        // 0.500
        assertEquals(nanosPerRequest, limiter.getDelayNanos("r1"));
        assertEquals(nanosPerRequest * 2, limiter.getDelayNanos("r1"));
        watch.advance(nanosPerRequest);
        // 0.750
        assertEquals(nanosPerRequest * 2, limiter.getDelayNanos("r1"));
        // 1.000
        watch.advance(nanosPerRequest);
        // 1.250
        watch.advance(nanosPerRequest);
        // 1.500
        assertEquals(nanosPerRequest, limiter.getDelayNanos("r1"));
        assertEquals(nanosPerRequest * 2, limiter.getDelayNanos("r1"));
        assertEquals(nanosPerRequest * 3, limiter.getDelayNanos("r1"));
        assertEquals(-nanosPerRequest, limiter.getDelayNanos("r1"));
        assertEquals(-nanosPerRequest, limiter.getDelayNanos("r1"));
        watch.advance(nanosPerRequest);
        // 1.750
        assertEquals(nanosPerRequest * 3, limiter.getDelayNanos("r1"));
        assertEquals(-nanosPerRequest, limiter.getDelayNanos("r1"));
        watch.advance(nanosPerRequest);
        // 2.000
        assertEquals(nanosPerRequest * 3, limiter.getDelayNanos("r1"));
        assertEquals(-nanosPerRequest, limiter.getDelayNanos("r1"));
        watch.advance(nanosPerRequest);
        // 2.250
        assertEquals(nanosPerRequest * 3, limiter.getDelayNanos("r1"));
        // 2.500 ...
    }

    @Test
    void should_return_delay() {
        int capacity = 3;
        Duration requestInterval = Duration.ofMillis(250);
        MockWatch watch = new MockWatch();
        var limiter = new LeakyBucketBlockingRateLimiter(capacity, requestInterval, watch);
        long nanosPerRequest = requestInterval.toNanos();
        assertEquals(0L, limiter.getDelayNanos("r1"));
        assertEquals(nanosPerRequest, limiter.getDelayNanos("r1"));
        assertEquals(nanosPerRequest * 2, limiter.getDelayNanos("r1"));
        assertEquals(nanosPerRequest * 3, limiter.getDelayNanos("r1"));
        assertEquals(-nanosPerRequest, limiter.getDelayNanos("r1"));
        assertEquals(-nanosPerRequest, limiter.getDelayNanos("r1"));
    }

}