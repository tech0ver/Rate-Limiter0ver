package io.github.tech0ver.ratelimiter.bucket;

import io.github.tech0ver.ratelimiter.MockedTimeSource;
import io.github.tech0ver.ratelimiter.MyRateLimiter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenBucketTest {

    static Stream<Arguments> provideLimiters() {
        return Stream.of(
                Arguments.argumentSet("ThreadUnsafe",
                        (Function<BucketConfig, MyRateLimiter>) ThreadUnsafeTokenBucket::new),
                Arguments.argumentSet("Synchronized",
                        (Function<BucketConfig, MyRateLimiter>) SynchronizedTokenBucket::new),
                Arguments.argumentSet("LockFree",
                        (Function<BucketConfig, MyRateLimiter>) LockFreeTokenBucket::new)
        );
    }

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
    @ParameterizedTest
    @MethodSource("provideLimiters")
    void should_enforce_refilling(Function<BucketConfig, MyRateLimiter> builder) {
        int capacity = 3;
        int refillTokens = 4;
        Duration refillPeriod = Duration.ofSeconds(1);
        MockedTimeSource watch = new MockedTimeSource();
        var limiter = builder.apply(new BucketConfig(capacity, refillTokens, refillPeriod, watch));
        long timeShift = refillPeriod.toNanos() / 4;
        // 0.000
        assertTrue(limiter.isGranted());
        watch.advance(timeShift);
        // 0.250
        assertTrue(limiter.isGranted());
        assertTrue(limiter.isGranted());
        watch.advance(timeShift);
        // 0.500
        assertTrue(limiter.isGranted());
        assertTrue(limiter.isGranted());
        watch.advance(timeShift);
        // 0.750
        assertTrue(limiter.isGranted());
        assertFalse(limiter.isGranted());
        watch.advance(timeShift);
        // 1.000
        watch.advance(timeShift);
        // 1.250
        watch.advance(timeShift);
        // 1.500
        watch.advance(timeShift);
        // 1.750
        assertTrue(limiter.isGranted());
        assertTrue(limiter.isGranted());
        assertTrue(limiter.isGranted());
        assertFalse(limiter.isGranted());
        watch.advance(timeShift);
        // 2.000
        assertTrue(limiter.isGranted());
        assertFalse(limiter.isGranted());
        watch.advance(timeShift);
        // 2.250
        assertTrue(limiter.isGranted());
        assertFalse(limiter.isGranted());
        watch.advance(timeShift);
        // 2.500 ...
    }

    @ParameterizedTest
    @MethodSource("provideLimiters")
    void should_permit(Function<BucketConfig, MyRateLimiter> builder) {
        int capacity = 2;
        int refillTokens = 2;
        Duration refillPeriod = Duration.ofSeconds(1);
        MockedTimeSource watch = new MockedTimeSource();
        var limiter = builder.apply(new BucketConfig(capacity, refillTokens, refillPeriod, watch));
        // 0.000
        var decision = limiter.tryPermit();
        assertTrue(decision.granted());
        assertTrue(decision.retryAfter().isEmpty());
        // 0.250
        watch.advance(Duration.ofMillis(250));
        decision = limiter.tryPermit();
        assertTrue(decision.granted());
        assertTrue(decision.retryAfter().isEmpty());
        // 0.500
        watch.advance(Duration.ofMillis(250));
        decision = limiter.tryPermit();
        assertTrue(decision.granted());
        assertTrue(decision.retryAfter().isEmpty());
        // 0.600
        watch.advance(Duration.ofMillis(100));
        decision = limiter.tryPermit();
        assertFalse(decision.granted());
        assertEquals(Duration.ofMillis(400), decision.retryAfter().get());
        // 0.900
        watch.advance(Duration.ofMillis(300));
        decision = limiter.tryPermit();
        assertFalse(decision.granted());
        assertEquals(Duration.ofMillis(100), decision.retryAfter().get());
        // 1.000
        watch.advance(Duration.ofMillis(100));
        decision = limiter.tryPermit();
        assertTrue(decision.granted());
        assertTrue(decision.retryAfter().isEmpty());
        // 1.100
        watch.advance(Duration.ofMillis(100));
        decision = limiter.tryPermit();
        assertFalse(decision.granted());
        assertEquals(Duration.ofMillis(400), decision.retryAfter().get());
    }

    @ParameterizedTest
    @MethodSource("provideLimiters")
    void should_return_remaining(Function<BucketConfig, MyRateLimiter> builder) {
        int capacity = 3;
        int refillTokens = 2;
        Duration refillPeriod = Duration.ofSeconds(1);
        MockedTimeSource watch = new MockedTimeSource();
        var limiter = builder.apply(new BucketConfig(capacity, refillTokens, refillPeriod, watch));
        // 0.000
        var decision = limiter.tryPermit();
        assertTrue(decision.granted());
        assertEquals(2L, decision.remaining());
        decision = limiter.tryPermit();
        assertTrue(decision.granted());
        assertEquals(1L, decision.remaining());
        decision = limiter.tryPermit();
        assertTrue(decision.granted());
        assertEquals(0L, decision.remaining());
        decision = limiter.tryPermit();
        assertFalse(decision.granted());
        assertEquals(0L, decision.remaining());
    }

}