package io.github.tech0ver.ratelimiter.bucket;

import io.github.tech0ver.ratelimiter.MockWatch;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeakyBucketRateLimiterTest {

    /**
     * <pre>
     *     t l  -  +
     * 0.000 0    +1
     *  .250 1 -1 +2
     *  .500 2 -1 +2
     *  .750 3 -1 +1
     * 1.000 3 -1
     *  .250 2 -1
     *  .500 1 -1
     *  .750 0    +3
     * 2.000 3 -1 +1
     *  .250 3 -1 +1
     *  .500 3 -1
     *  .750 2 -1
     * 3.000 1 -1
     * </pre>
     */
    @Test
    void should_enforce_leaking() {
        int capacity = 3;
        int leakCount = 4;
        Duration leakPeriod = Duration.ofSeconds(1);
        MockWatch watch = new MockWatch();
        var limiter = new LeakyBucketRateLimiter(capacity, leakCount, leakPeriod, watch);
        long timeShift = leakPeriod.toNanos() / 4;
        // 0.000
        assertTrue(limiter.isAllowed("r1"));
        watch.advance(timeShift);
        // 0.250
        assertTrue(limiter.isAllowed("r1"));
        assertTrue(limiter.isAllowed("r1"));
        watch.advance(timeShift);
        // 0.500
        assertTrue(limiter.isAllowed("r1"));
        assertTrue(limiter.isAllowed("r1"));
        watch.advance(timeShift);
        // 0.750
        assertTrue(limiter.isAllowed("r1"));
        assertFalse(limiter.isAllowed("r1"));
        watch.advance(timeShift);
        // 1.000
        watch.advance(timeShift);
        // 1.250
        watch.advance(timeShift);
        // 1.500
        watch.advance(timeShift);
        // 1.750
        assertTrue(limiter.isAllowed("r1"));
        assertTrue(limiter.isAllowed("r1"));
        assertTrue(limiter.isAllowed("r1"));
        assertFalse(limiter.isAllowed("r1"));
        watch.advance(timeShift);
        // 2.000
        assertTrue(limiter.isAllowed("r1"));
        assertFalse(limiter.isAllowed("r1"));
        watch.advance(timeShift);
        // 2.250
        assertTrue(limiter.isAllowed("r1"));
        assertFalse(limiter.isAllowed("r1"));
        watch.advance(timeShift);
        // 2.500 ...
    }

    @Test
    void should_decide() {
        int capacity = 2;
        int leakCount = 2;
        Duration leakPeriod = Duration.ofSeconds(1);
        MockWatch watch = new MockWatch();
        var limiter = new LeakyBucketRateLimiter(capacity, leakCount, leakPeriod, watch);
        // 0.000
        var decision = limiter.decide("r1");
        assertTrue(decision.isAllowed());
        assertEquals(Duration.ZERO, decision.retryAfter());
        // 0.250
        watch.advance(Duration.ofMillis(250));
        decision = limiter.decide("r1");
        assertTrue(decision.isAllowed());
        assertEquals(Duration.ZERO, decision.retryAfter());
        // 0.500
        watch.advance(Duration.ofMillis(250));
        decision = limiter.decide("r1");
        assertTrue(decision.isAllowed());
        assertEquals(Duration.ZERO, decision.retryAfter());
        // 0.600
        watch.advance(Duration.ofMillis(100));
        decision = limiter.decide("r1");
        assertFalse(decision.isAllowed());
        assertEquals(Duration.ofMillis(400), decision.retryAfter());
        // 0.900
        watch.advance(Duration.ofMillis(300));
        decision = limiter.decide("r1");
        assertFalse(decision.isAllowed());
        assertEquals(Duration.ofMillis(100).plusNanos(1), decision.retryAfter());
        // 1.000
        watch.advance(Duration.ofMillis(100));
        decision = limiter.decide("r1");
        assertTrue(decision.isAllowed());
        assertEquals(Duration.ZERO, decision.retryAfter());
        // 1.100
        watch.advance(Duration.ofMillis(100));
        decision = limiter.decide("r1");
        assertFalse(decision.isAllowed());
        assertEquals(Duration.ofMillis(400), decision.retryAfter());
        // 1.400
        watch.advance(Duration.ofMillis(300));
        decision = limiter.decide("r1");
        assertFalse(decision.isAllowed());
        assertEquals(Duration.ofMillis(100).plusNanos(1), decision.retryAfter());
        // 1.500
        watch.advance(Duration.ofMillis(100));
        decision = limiter.decide("r1");
        assertTrue(decision.isAllowed());
        assertEquals(Duration.ZERO, decision.retryAfter());
    }

}