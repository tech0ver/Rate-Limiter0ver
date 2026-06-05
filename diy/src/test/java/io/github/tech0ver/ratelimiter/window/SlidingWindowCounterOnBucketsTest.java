package io.github.tech0ver.ratelimiter.window;

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

class SlidingWindowCounterOnBucketsTest {

    static Stream<Arguments> provideLimiters() {
        return Stream.of(
                Arguments.argumentSet("ThreadUnsafe",
                        (Function<BucketedWindowConfig, MyRateLimiter>) ThreadUnsafeSlidingWindowCounterOnBuckets::new),
                Arguments.argumentSet("Synchronized",
                        (Function<BucketedWindowConfig, MyRateLimiter>) SynchronizedSlidingWindowCounterOnBuckets::new),
                Arguments.argumentSet("LockFree",
                        (Function<BucketedWindowConfig, MyRateLimiter>) LockFreeSlidingWindowCounterOnBuckets::new)
        );
    }

    /**
     * <pre>
     * |           |           |
     * |           |           |
     * |         - |         - |
     * | + + + + + | + + + + + |
     * | + + + + + | + + + + + |
     * | + + + + + | + + + + + |
     * </pre>
     */
    @ParameterizedTest
    @MethodSource("provideLimiters")
    void should_enforce_limit_with_uniformLoad_in_bothWindows(Function<BucketedWindowConfig, MyRateLimiter> builder) {
        int limit = 15;
        var window = Duration.ofSeconds(1);
        int numberOfBuckets = 5;
        var watch = new MockedTimeSource();
        var limiter = builder.apply(new BucketedWindowConfig(limit, window, numberOfBuckets, watch));
        // window 0
        for (int bucket = 0; bucket < numberOfBuckets; bucket++) {
            for (int request = 0; request < limit / numberOfBuckets; request++) {
                assertTrue(
                        limiter.isGranted(),
                        "Window #0 Bucket #" + bucket + " Request #" + request
                );
            }
            if (bucket + 1 == numberOfBuckets) {
                assertFalse(
                        limiter.isGranted(),
                        "Window #0 Bucket #" + bucket + " Last request"
                );
            }
            watch.advance(window.toNanos() / numberOfBuckets);
        }
        // window 1
        for (int bucket = 0; bucket < numberOfBuckets; bucket++) {
            for (int request = 0; request < limit / numberOfBuckets; request++) {
                assertTrue(
                        limiter.isGranted(),
                        "Window #1 Bucket #" + bucket + " Request #" + request
                );
            }
            if (bucket + 1 == numberOfBuckets) {
                assertFalse(
                        limiter.isGranted(),
                        "Window #1 Bucket #" + bucket + " Last request"
                );
            }
            watch.advance(window.toNanos() / numberOfBuckets);
        }
    }

    /**
     * <pre>
     * |         - |           |
     * |         + | -         |
     * |       + + | - -       |
     * |     + + + | - - +     |
     * |   + + + + | - + + + + |
     * | + + + + + | + + + + + |
     * </pre>
     */
    @ParameterizedTest
    @MethodSource("provideLimiters")
    void should_enforce_limit_with_ascThenDescLoad(Function<BucketedWindowConfig, MyRateLimiter> builder) {
        int limit = 15;
        var window = Duration.ofSeconds(1);
        var watch = new MockedTimeSource();
        int numberOfBuckets = 5;
        var limiter = builder.apply(new BucketedWindowConfig(limit, window, numberOfBuckets, watch));
        // window 0
        for (int bucket = 0; bucket < numberOfBuckets; bucket++) {
            for (int request = 0; request <= bucket; request++) {
                assertTrue(
                        limiter.isGranted(),
                        "Window #0 Bucket #" + bucket + " Request #" + request
                );
            }
            if (bucket + 1 == numberOfBuckets) {
                assertFalse(
                        limiter.isGranted(),
                        "Window #0 Bucket #" + bucket + " Last request"
                );
            }
            watch.advance(window.toNanos() / numberOfBuckets);
        }
        // window 1
        // bucket 0
        for (int request = 0; request < numberOfBuckets; request++) {
            if (request < 1) {
                assertTrue(
                        limiter.isGranted(),
                        "Window #1 Bucket #0 Request #" + request
                );
            } else {
                assertFalse(
                        limiter.isGranted(),
                        "Window #1 Bucket #0 Request #" + request
                );
            }
        }
        watch.advance(window.toNanos() / numberOfBuckets);
        // bucket 1
        for (int request = 0; request < numberOfBuckets - 1; request++) {
            if (request < 2) {
                assertTrue(
                        limiter.isGranted(),
                        "Window #1 Bucket #1 Request #" + request
                );
            } else {
                assertFalse(
                        limiter.isGranted(),
                        "Window #1 Bucket #1 Request #" + request
                );
            }
        }
        watch.advance(window.toNanos() / numberOfBuckets);
        // bucket 2-4
        for (int bucket = numberOfBuckets - 3; bucket >= 0; bucket--) {
            for (int request = 0; request <= bucket; request++) {
                assertTrue(
                        limiter.isGranted(),
                        "Window #1 Bucket #" + (numberOfBuckets - bucket) + " Request #" + request
                );
            }
            if (bucket == 0) {
                assertTrue(
                        limiter.isGranted(),
                        "Window #1 Bucket #" + (numberOfBuckets - bucket) + " Last request"
                );
            }
            watch.advance(window.toNanos() / numberOfBuckets);
        }
    }

    /**
     * <pre>
     * |           |         - |
     * | +         |         + |
     * | + +       |       + + |
     * | + + +     |     + + + |
     * | + + + + - |   + + + + |
     * | + + + + + | + + + + + |
     * </pre>
     */
    @ParameterizedTest
    @MethodSource("provideLimiters")
    void should_enforce_limit_with_descThenAscLoad(Function<BucketedWindowConfig, MyRateLimiter> builder) {
        int limit = 15;
        var window = Duration.ofSeconds(1);
        var watch = new MockedTimeSource();
        int numberOfBuckets = 5;
        var limiter = builder.apply(new BucketedWindowConfig(limit, window, numberOfBuckets, watch));
        // window 0
        for (int bucket = numberOfBuckets - 1; bucket >= 0; bucket--) {
            for (int request = 0; request <= bucket; request++) {
                assertTrue(
                        limiter.isGranted(),
                        "Window #0 Bucket #" + (numberOfBuckets - bucket) + " Request #" + request
                );
            }
            if (bucket == 0) {
                assertFalse(
                        limiter.isGranted(),
                        "Window #0 Bucket #" + (numberOfBuckets - bucket) + " Last request"
                );
            }
            watch.advance(window.toNanos() / numberOfBuckets);
        }
        // window 1
        for (int bucket = 0; bucket < numberOfBuckets; bucket++) {
            for (int request = 0; request <= bucket; request++) {
                assertTrue(
                        limiter.isGranted(),
                        "Window #1 Bucket #" + bucket + " Request #" + request
                );
            }
            if (bucket + 1 == numberOfBuckets) {
                assertFalse(
                        limiter.isGranted(),
                        "Window #1 Bucket #" + bucket + " Last request"
                );
            }
            watch.advance(window.toNanos() / numberOfBuckets);
        }
    }

    /**
     * <pre>
     * |           |           |
     * |     +     |     +     |
     * |     +     |     +     |
     * |   + + + - |   + + + - |
     * | + + + + + | + + + + + |
     * | + + + + + | + + + + + |
     * </pre>
     */
    @ParameterizedTest
    @MethodSource("provideLimiters")
    void should_enforce_limit_with_centralPeakLoad_in_bothWindows(Function<BucketedWindowConfig, MyRateLimiter> builder) {
        int limit = 15;
        var window = Duration.ofSeconds(1);
        var watch = new MockedTimeSource();
        int numberOfBuckets = 5;
        var limiter = builder.apply(new BucketedWindowConfig(limit, window, numberOfBuckets, watch));
        // window 0
        for (int bucket = 0; bucket < numberOfBuckets; bucket++) {
            if (bucket == 0 || bucket + 1 == numberOfBuckets) {
                for (int i = 0; i < 2; i++) {
                    assertTrue(
                            limiter.isGranted(),
                            "Window #0 Bucket #" + bucket + " Request #" + i
                    );
                }
            } else if (bucket == 1 || bucket + 2 == numberOfBuckets) {
                for (int i = 0; i < 3; i++) {
                    assertTrue(
                            limiter.isGranted(),
                            "Window #0 Bucket #" + bucket + " Request #" + i
                    );
                }
            } else {
                for (int i = 0; i < 5; i++) {
                    assertTrue(
                            limiter.isGranted(),
                            "Window #0 Bucket #" + bucket + " Request #" + i
                    );
                }
            }
            if (bucket + 1 == numberOfBuckets) {
                assertFalse(
                        limiter.isGranted(),
                        "Window #0 Bucket #" + bucket + " Last request"
                );
            }
            watch.advance(window.toNanos() / numberOfBuckets);
        }
        // window 1
        for (int bucket = 0; bucket < numberOfBuckets; bucket++) {
            if (bucket == 0 || bucket + 1 == numberOfBuckets) {
                for (int i = 0; i < 2; i++) {
                    assertTrue(
                            limiter.isGranted(),
                            "Window #1 Bucket #" + bucket + " Request #" + i
                    );
                }
            } else if (bucket == 1 || bucket + 2 == numberOfBuckets) {
                for (int i = 0; i < 3; i++) {
                    assertTrue(
                            limiter.isGranted(),
                            "Window #1 Bucket #" + bucket + " Request #" + i
                    );
                }
            } else {
                for (int i = 0; i < 5; i++) {
                    assertTrue(
                            limiter.isGranted(),
                            "Window #1 Bucket #" + bucket + " Request #" + i
                    );
                }
            }
            if (bucket + 1 == numberOfBuckets) {
                assertFalse(
                        limiter.isGranted(),
                        "Window #1 Bucket #" + bucket + " Last request"
                );
            }
            watch.advance(window.toNanos() / numberOfBuckets);
        }
    }

    @ParameterizedTest
    @MethodSource("provideLimiters")
    void should_return_retryAfter(Function<BucketedWindowConfig, MyRateLimiter> builder) {
        int limit = 2;
        var window = Duration.ofSeconds(1);
        var watch = new MockedTimeSource();
        int numberOfBuckets = 2;
        var limiter = builder.apply(new BucketedWindowConfig(limit, window, numberOfBuckets, watch));
        // 0.000
        var decision = limiter.tryPermit();
        assertTrue(decision.granted());
        assertTrue(decision.retryAfter().isEmpty());
        // 0.500
        watch.advance(Duration.ofMillis(500));
        decision = limiter.tryPermit();
        assertTrue(decision.granted());
        assertTrue(decision.retryAfter().isEmpty());
        // 0.750
        watch.advance(Duration.ofMillis(250));
        decision = limiter.tryPermit();
        assertFalse(decision.granted());
        assertEquals(Duration.ofMillis(250), decision.retryAfter().get());
        // 1.000
        watch.advance(Duration.ofMillis(250));
        decision = limiter.tryPermit();
        assertTrue(decision.granted());
        assertTrue(decision.retryAfter().isEmpty());
        // 1.250
        watch.advance(Duration.ofMillis(250));
        decision = limiter.tryPermit();
        assertFalse(decision.granted());
        assertEquals(Duration.ofMillis(250), decision.retryAfter().get());
        // 1.500
        watch.advance(Duration.ofMillis(250));
        decision = limiter.tryPermit();
        assertTrue(decision.granted());
        assertTrue(decision.retryAfter().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("provideLimiters")
    void should_return_remaining(Function<BucketedWindowConfig, MyRateLimiter> builder) {
        int limit = 3;
        var window = Duration.ofSeconds(1);
        var watch = new MockedTimeSource();
        int numberOfBuckets = 2;
        var limiter = builder.apply(new BucketedWindowConfig(limit, window, numberOfBuckets, watch));
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