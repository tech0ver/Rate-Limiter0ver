package io.github.tech0ver.ratelimiter.bucket;

import io.github.tech0ver.ratelimiter.MockWatch;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenBucketRateLimiterTest {

    /**
     * <pre>
     *     t c  +  -
     * 0.000 3    -1
     *  .250 2 +1 -2
     *  .500 1 +1 -2
     *  .750 0 +1 -1
     * 1.000 0 +1
     *  .250 1 +1
     *  .500 2 +1
     *  .750 3    -3
     * 2.000 0 +1 -1
     *  .250 0 +1 -1
     *  .500 0 +1
     *  .750 1 +1
     * 3.000 2 +1
     * </pre>
     */
    @Test
    void should_enforce_refilling() {
        int capacity = 3;
        int refillTokens = 4;
        Duration refillPeriod = Duration.ofSeconds(1);
        MockWatch watch = new MockWatch();
        var limiter = new TokenBucketRateLimiter(capacity, refillTokens, refillPeriod, watch);
        long timeShift = refillPeriod.toNanos() / 4;
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
        int refillTokens = 2;
        Duration refillPeriod = Duration.ofSeconds(1);
        MockWatch watch = new MockWatch();
        var limiter = new TokenBucketRateLimiter(capacity, refillTokens, refillPeriod, watch);
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
        assertEquals(Duration.ofMillis(100), decision.retryAfter());
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
    }

}