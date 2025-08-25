package io.github.tech0ver.ratelimiter.window;

import io.github.tech0ver.ratelimiter.MockWatch;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlidingWindowLogRateLimiterTest {

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
    @Test
    void should_enforce_limit_with_uniformLoad_in_bothWindows() {
        int limit = 15;
        var window = Duration.ofSeconds(1);
        var watch = new MockWatch();
        var limiter = new SlidingWindowLogRateLimiter(limit, window, watch);
        int numberOfBuckets = 5;
        // window 0
        for (int bucket = 0; bucket < numberOfBuckets; bucket++) {
            for (int request = 0; request < limit / numberOfBuckets; request++) {
                assertTrue(
                        limiter.isAllowed("r1"),
                        "Window #0 Bucket #" + bucket + " Request #" + request
                );
            }
            if (bucket + 1 == numberOfBuckets) {
                assertFalse(
                        limiter.isAllowed("r1"),
                        "Window #0 Bucket #" + bucket + " Last request"
                );
            }
            watch.advance(window.toNanos() / numberOfBuckets);
        }
        // window 1
        for (int bucket = 0; bucket < numberOfBuckets; bucket++) {
            for (int request = 0; request < limit / numberOfBuckets; request++) {
                assertTrue(
                        limiter.isAllowed("r1"),
                        "Window #1 Bucket #" + bucket + " Request #" + request
                );
            }
            if (bucket + 1 == numberOfBuckets) {
                assertFalse(
                        limiter.isAllowed("r1"),
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
    @Test
    void should_enforce_limit_with_ascThenDescLoad() {
        int limit = 15;
        var window = Duration.ofSeconds(1);
        var watch = new MockWatch();
        var limiter = new SlidingWindowLogRateLimiter(limit, window, watch);
        int numberOfBuckets = 5;
        // window 0
        for (int bucket = 0; bucket < numberOfBuckets; bucket++) {
            for (int request = 0; request <= bucket; request++) {
                assertTrue(
                        limiter.isAllowed("r1"),
                        "Window #0 Bucket #" + bucket + " Request #" + request
                );
            }
            if (bucket + 1 == numberOfBuckets) {
                assertFalse(
                        limiter.isAllowed("r1"),
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
                        limiter.isAllowed("r1"),
                        "Window #1 Bucket #0 Request #" + request
                );
            } else {
                assertFalse(
                        limiter.isAllowed("r1"),
                        "Window #1 Bucket #0 Request #" + request
                );
            }
        }
        watch.advance(window.toNanos() / numberOfBuckets);
        // bucket 1
        for (int request = 0; request < numberOfBuckets - 1; request++) {
            if (request < 2) {
                assertTrue(
                        limiter.isAllowed("r1"),
                        "Window #1 Bucket #1 Request #" + request
                );
            } else {
                assertFalse(
                        limiter.isAllowed("r1"),
                        "Window #1 Bucket #1 Request #" + request
                );
            }
        }
        watch.advance(window.toNanos() / numberOfBuckets);
        // bucket 2-4
        for (int bucket = numberOfBuckets - 3; bucket >= 0; bucket--) {
            for (int request = 0; request <= bucket; request++) {
                assertTrue(
                        limiter.isAllowed("r1"),
                        "Window #1 Bucket #" + (numberOfBuckets - bucket) + " Request #" + request
                );
            }
            if (bucket == 0) {
                assertTrue(
                        limiter.isAllowed("r1"),
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
    @Test
    void should_enforce_limit_with_descThenAscLoad() {
        int limit = 15;
        var window = Duration.ofSeconds(1);
        var watch = new MockWatch();
        var limiter = new SlidingWindowLogRateLimiter(limit, window, watch);
        int numberOfBuckets = 5;
        // window 0
        for (int bucket = numberOfBuckets - 1; bucket >= 0; bucket--) {
            for (int request = 0; request <= bucket; request++) {
                assertTrue(
                        limiter.isAllowed("r1"),
                        "Window #0 Bucket #" + (numberOfBuckets - bucket) + " Request #" + request
                );
            }
            if (bucket == 0) {
                assertFalse(
                        limiter.isAllowed("r1"),
                        "Window #0 Bucket #" + (numberOfBuckets - bucket) + " Last request"
                );
            }
            watch.advance(window.toNanos() / numberOfBuckets);
        }
        // window 1
        for (int bucket = 0; bucket < numberOfBuckets; bucket++) {
            for (int request = 0; request <= bucket; request++) {
                assertTrue(
                        limiter.isAllowed("r1"),
                        "Window #1 Bucket #" + bucket + " Request #" + request
                );
            }
            if (bucket + 1 == numberOfBuckets) {
                assertFalse(
                        limiter.isAllowed("r1"),
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
    @Test
    void should_enforce_limit_with_centralPeakLoad_in_bothWindows() {
        int limit = 15;
        var window = Duration.ofSeconds(1);
        var watch = new MockWatch();
        var limiter = new SlidingWindowLogRateLimiter(limit, window, watch);
        int numberOfBuckets = 5;
        // window 0
        for (int bucket = 0; bucket < numberOfBuckets; bucket++) {
            if (bucket == 0 || bucket + 1 == numberOfBuckets) {
                for (int i = 0; i < 2; i++) {
                    assertTrue(
                            limiter.isAllowed("r1"),
                            "Window #0 Bucket #" + bucket + " Request #" + i
                    );
                }
            } else if (bucket == 1 || bucket + 2 == numberOfBuckets) {
                for (int i = 0; i < 3; i++) {
                    assertTrue(
                            limiter.isAllowed("r1"),
                            "Window #0 Bucket #" + bucket + " Request #" + i
                    );
                }
            } else {
                for (int i = 0; i < 5; i++) {
                    assertTrue(
                            limiter.isAllowed("r1"),
                            "Window #0 Bucket #" + bucket + " Request #" + i
                    );
                }
            }
            if (bucket + 1 == numberOfBuckets) {
                assertFalse(
                        limiter.isAllowed("r1"),
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
                            limiter.isAllowed("r1"),
                            "Window #1 Bucket #" + bucket + " Request #" + i
                    );
                }
            } else if (bucket == 1 || bucket + 2 == numberOfBuckets) {
                for (int i = 0; i < 3; i++) {
                    assertTrue(
                            limiter.isAllowed("r1"),
                            "Window #1 Bucket #" + bucket + " Request #" + i
                    );
                }
            } else {
                for (int i = 0; i < 5; i++) {
                    assertTrue(
                            limiter.isAllowed("r1"),
                            "Window #1 Bucket #" + bucket + " Request #" + i
                    );
                }
            }
            if (bucket + 1 == numberOfBuckets) {
                assertFalse(
                        limiter.isAllowed("r1"),
                        "Window #1 Bucket #" + bucket + " Last request"
                );
            }
            watch.advance(window.toNanos() / numberOfBuckets);
        }
    }

}