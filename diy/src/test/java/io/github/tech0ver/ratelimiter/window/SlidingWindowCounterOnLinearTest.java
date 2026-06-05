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

class SlidingWindowCounterOnLinearTest {

    static Stream<Arguments> provideLimiters() {
        return Stream.of(
                Arguments.argumentSet("ThreadUnsafe",
                        (Function<WindowConfig, MyRateLimiter>) ThreadUnsafeSlidingWindowCounterOnLinear::new),
                Arguments.argumentSet("Synchronized",
                        (Function<WindowConfig, MyRateLimiter>) SynchronizedSlidingWindowCounterOnLinear::new),
                Arguments.argumentSet("LockFree",
                        (Function<WindowConfig, MyRateLimiter>) LockFreeSlidingWindowCounterOnLinear::new)
        );
    }

    /**
     * <pre>
     * |           |           |
     * |           |           |
     * |         - |         - |
     * | + + + + + | - + + + + |
     * | + + + + + | - + + + + |
     * | + + + + + | - + + + + |
     * </pre>
     */
    @ParameterizedTest
    @MethodSource("provideLimiters")
    void should_enforce_limit_with_uniformLoad_in_bothWindows(Function<WindowConfig, MyRateLimiter> builder) {
        int limit = 15;
        var window = Duration.ofSeconds(1);
        var watch = new MockedTimeSource();
        var limiter = builder.apply(new WindowConfig(limit, window, watch));
        int numberOfBuckets = 5;
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
                if (bucket != 0) {
                    assertTrue(
                            limiter.isGranted(),
                            "Window #1 Bucket #" + bucket + " Request #" + request
                    );
                } else {
                    assertFalse(
                            limiter.isGranted(),
                            "Window #1 Bucket #" + bucket + " Request #" + request
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

    /**
     * <pre>
     * |         - |           |
     * |         + | -         |
     * |       + + | - -       |
     * |     + + + | - + +     |
     * |   + + + + | - + + + + |
     * | + + + + + | - + + + + |
     * </pre>
     */
    @ParameterizedTest
    @MethodSource("provideLimiters")
    void should_enforce_limit_with_ascThenDescLoad(Function<WindowConfig, MyRateLimiter> builder) {
        int limit = 15;
        var window = Duration.ofSeconds(1);
        var watch = new MockedTimeSource();
        var limiter = builder.apply(new WindowConfig(limit, window, watch));
        int numberOfBuckets = 5;
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
            assertFalse(
                    limiter.isGranted(),
                    "Window #1 Bucket #0 Request #" + request
            );
        }
        watch.advance(window.toNanos() / numberOfBuckets);
        // bucket 1
        for (int request = 0; request < numberOfBuckets - 1; request++) {
            if (request < 3) {
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
     * | +         |         - |
     * | + +       |       + - |
     * | + + +     |     + + + |
     * | + + + + - |   + + + + |
     * | + + + + + | - + + + + |
     * </pre>
     */
    @ParameterizedTest
    @MethodSource("provideLimiters")
    void should_enforce_limit_with_descThenAscLoad(Function<WindowConfig, MyRateLimiter> builder) {
        int limit = 15;
        var window = Duration.ofSeconds(1);
        var watch = new MockedTimeSource();
        var limiter = builder.apply(new WindowConfig(limit, window, watch));
        int numberOfBuckets = 5;
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
                if (bucket == 0 || (bucket == 4 && request > 2)) {
                    assertFalse(
                            limiter.isGranted(),
                            "Window #1 Bucket #" + bucket + " Request #" + request
                    );
                } else {
                    assertTrue(
                            limiter.isGranted(),
                            "Window #1 Bucket #" + bucket + " Request #" + request
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

    /**
     * <pre>
     * |           |           |
     * |     +     |     -     |
     * |     +     |     -     |
     * |   + + + - |   + + + + |
     * | + + + + + | - + + + + |
     * | + + + + + | - + + + + |
     * </pre>
     */
    @ParameterizedTest
    @MethodSource("provideLimiters")
    void should_enforce_limit_with_centralPeakLoad_in_bothWindows(Function<WindowConfig, MyRateLimiter> builder) {
        int limit = 15;
        var window = Duration.ofSeconds(1);
        var watch = new MockedTimeSource();
        var limiter = builder.apply(new WindowConfig(limit, window, watch));
        int numberOfBuckets = 5;
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
            if (bucket == 0) {
                for (int request = 0; request < 2; request++) {
                    assertFalse(
                            limiter.isGranted(),
                            "Window #1 Bucket #" + bucket + " Request #" + request
                    );
                }
            } else if (bucket + 1 == numberOfBuckets) {
                for (int request = 0; request < 2; request++) {
                    assertTrue(
                            limiter.isGranted(),
                            "Window #1 Bucket #" + bucket + " Request #" + request
                    );
                }
            } else if (bucket == 1 || bucket + 2 == numberOfBuckets) {
                for (int request = 0; request < 3; request++) {
                    assertTrue(
                            limiter.isGranted(),
                            "Window #1 Bucket #" + bucket + " Request #" + request
                    );
                }
            } else {
                for (int request = 0; request < 5; request++) {
                    if (request < 3) {
                        assertTrue(
                                limiter.isGranted(),
                                "Window #1 Bucket #" + bucket + " Request #" + request
                        );
                    } else {
                        assertFalse(
                                limiter.isGranted(),
                                "Window #1 Bucket #" + bucket + " Request #" + request
                        );
                    }
                }
            }
            if (bucket + 1 == numberOfBuckets) {
                assertTrue(
                        limiter.isGranted(),
                        "Window #1 Bucket #" + bucket + " Last request"
                );
            }
            watch.advance(window.toNanos() / numberOfBuckets);
        }
    }

    @ParameterizedTest
    @MethodSource("provideLimiters")
    void should_return_retryAfter(Function<WindowConfig, MyRateLimiter> builder) {
        int limit = 2;
        var window = Duration.ofSeconds(1);
        var watch = new MockedTimeSource();
        var limiter = builder.apply(new WindowConfig(limit, window, watch));
        // 0.000
        var decision = limiter.tryPermit();
        assertTrue(decision.granted());
        assertTrue(decision.retryAfter().isEmpty());
        // 0.300
        watch.advance(Duration.ofMillis(300));
        decision = limiter.tryPermit();
        assertTrue(decision.granted());
        assertTrue(decision.retryAfter().isEmpty());
        // 0.600
        watch.advance(Duration.ofMillis(300));
        decision = limiter.tryPermit();
        assertFalse(decision.granted());
        assertEquals(Duration.ofMillis(400), decision.retryAfter().get());
        // 0.900
        watch.advance(Duration.ofMillis(300));
        decision = limiter.tryPermit();
        assertFalse(decision.granted());
        assertEquals(Duration.ofMillis(100), decision.retryAfter().get());
        // 1.200
        watch.advance(Duration.ofMillis(300));
        decision = limiter.tryPermit();
        assertTrue(decision.granted());
        assertTrue(decision.retryAfter().isEmpty());
        // 1.400
        watch.advance(Duration.ofMillis(200));
        decision = limiter.tryPermit();
        assertFalse(decision.granted());
        assertEquals(Duration.ofMillis(100).plusNanos(1), decision.retryAfter().get());
        // 1.500
        watch.advance(Duration.ofMillis(100));
        decision = limiter.tryPermit();
        assertFalse(decision.granted());
        assertEquals(Duration.ofNanos(1), decision.retryAfter().get());
        // 1.500 + 1 nano
        watch.advance(Duration.ofNanos(1));
        decision = limiter.tryPermit();
        assertTrue(decision.granted());
        assertTrue(decision.retryAfter().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("provideLimiters")
    void should_return_remaining(Function<WindowConfig, MyRateLimiter> builder) {
        int limit = 3;
        var window = Duration.ofSeconds(1);
        var watch = new MockedTimeSource();
        var limiter = builder.apply(new WindowConfig(limit, window, watch));
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