package io.github.tech0ver.ratelimiter.window;

import io.github.tech0ver.ratelimiter.MyRateLimiter;
import io.github.tech0ver.ratelimiter.MyWatch;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SlidingWindowCounterRateLimiter implements MyRateLimiter {

    protected final MyWatch watch;
    protected final long limit;
    protected final long windowSizeNanos;

    protected SlidingWindowCounterRateLimiter(long limit, Duration windowSize, MyWatch watch) {
        if (limit <= 0) throw new IllegalArgumentException("limit must be > 0");
        Objects.requireNonNull(windowSize, "No windowSize");
        Objects.requireNonNull(watch, "No watch");
        this.watch = watch;
        this.limit = limit;
        this.windowSizeNanos = windowSize.toNanos();
    }

    public static SlidingWindowCounterRateLimiter createBucketed(
            long limit, Duration windowSize, int numberOfBuckets
    ) {
        return createBucketed(limit, windowSize, numberOfBuckets, MyWatch.SYSTEM_NANO_TIME);
    }

    public static SlidingWindowCounterRateLimiter createBucketed(
            long limit, Duration windowSize, int numberOfBuckets, MyWatch watch
    ) {
        return new BucketedCounter(limit, windowSize, numberOfBuckets, watch);
    }

    public static SlidingWindowCounterRateLimiter createLinearInterpolated(
            long limit, Duration windowSize
    ) {
        return createLinearInterpolated(limit, windowSize, MyWatch.SYSTEM_NANO_TIME);
    }

    public static SlidingWindowCounterRateLimiter createLinearInterpolated(
            long limit, Duration windowSize, MyWatch watch
    ) {
        return new LinearInterpolatedCounter(limit, windowSize, watch);
    }

    /**
     * Based on buckets.
     */
    private static class BucketedCounter extends SlidingWindowCounterRateLimiter {

        // O(N * B) where N is the number of active resources, B is the number of buckets
        private final Map<String, BucketedWindow> windowByResource;
        private final int numberOfBuckets;
        private final long bucketSizeNanos;

        BucketedCounter(long limit, Duration windowSize, int numberOfBuckets, MyWatch watch) {
            super(limit, windowSize, watch);
            if (numberOfBuckets <= 0) throw new IllegalArgumentException("numberOfBuckets must be > 0");
            this.numberOfBuckets = numberOfBuckets;
            this.bucketSizeNanos = windowSizeNanos / numberOfBuckets;
            this.windowByResource = new ConcurrentHashMap<>();
        }

        // O(B) where B is the number of buckets
        @Override
        public Decision decide(String resource) {
            Objects.requireNonNull(resource, "No resource");
            long nowNanos = watch.currentTimeNanos();
            // Get or create bucket
            BucketedWindow window = windowByResource.computeIfAbsent(resource, k -> new BucketedWindow());
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (window) {
                int bucketIndex = (int) ((nowNanos / bucketSizeNanos) % numberOfBuckets);
                long bucketStartNanos = nowNanos - (nowNanos % bucketSizeNanos);
                // Has the bucket expired?
                if (nowNanos - window.timestamps[bucketIndex] >= bucketSizeNanos) {
                    window.counts[bucketIndex] = 0;
                    window.timestamps[bucketIndex] = bucketStartNanos;
                }
                long totalCount = 0;
                int earliestExpiryBucketIndex = bucketIndex;
                for (int i = 0; i < numberOfBuckets; i++) {
                    // Is a bucket in the current window?
                    if (nowNanos - window.timestamps[i] < windowSizeNanos && window.counts[i] > 0) {
                        totalCount += window.counts[i];
                        if (window.timestamps[i] < window.timestamps[earliestExpiryBucketIndex]) {
                            earliestExpiryBucketIndex = i;
                        }
                    }
                }
                if (totalCount < limit) {
                    window.counts[bucketIndex]++;
                    return Decision.fromNanos(true, 0L);
                }
                long remainingNanos = windowSizeNanos - (nowNanos - window.timestamps[earliestExpiryBucketIndex]);
                return Decision.fromNanos(false, Math.max(remainingNanos, 0L));
            }
        }

        private final class BucketedWindow {

            long[] counts;
            long[] timestamps;

            private BucketedWindow() {
                this.counts = new long[numberOfBuckets];
                this.timestamps = new long[numberOfBuckets];
            }

        }

    }

    /**
     * Based on <a href="https://en.wikipedia.org/wiki/Linear_interpolation">
     * Linear Interpolation
     * </a>.
     */
    private static class LinearInterpolatedCounter extends SlidingWindowCounterRateLimiter {

        // O(N) where N is the number of active resources
        private final Map<String, Window> windowByResource;

        LinearInterpolatedCounter(long limit, Duration windowSize, MyWatch watch) {
            super(limit, windowSize, watch);
            this.windowByResource = new ConcurrentHashMap<>();
        }

        // O(1)
        @Override
        public Decision decide(String resource) {
            Objects.requireNonNull(resource, "No resource");
            long nowNanos = watch.currentTimeNanos();
            long currentWindowStartNanos = nowNanos - (nowNanos % windowSizeNanos);
            // Get or create window
            Window window = windowByResource.computeIfAbsent(resource, k -> new Window(currentWindowStartNanos));
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (window) {
                // Different windows?
                if (currentWindowStartNanos > window.startNanos) {
                    // Shift window
                    long windowsPassed = (currentWindowStartNanos - window.startNanos) / windowSizeNanos;
                    window.previousCount = windowsPassed == 1 ? window.currentCount : 0;
                    window.currentCount = 0;
                    window.startNanos = currentWindowStartNanos;
                }
                // double : prev * (1 - elapsed / W) + cur < limit
                // long   : prev * (W - elapsed) + cur * W < limit * W
                long currentWindowElapsedNanos = nowNanos - window.startNanos;
                long currentWindowRemainingNanos = windowSizeNanos - currentWindowElapsedNanos;
                long slidingNanos = window.previousCount * currentWindowRemainingNanos
                                    + window.currentCount * windowSizeNanos;
                long limitNanos = limit * windowSizeNanos;
                if (slidingNanos < limitNanos) {
                    window.currentCount++;
                    return Decision.fromNanos(true, 0L);
                }
                long remainingNanos;
                if (window.previousCount > 0) {
                    long overflowNanos = slidingNanos - limitNanos;
                    long waitNanos = (overflowNanos / window.previousCount) + 1;
                    remainingNanos = Math.min(waitNanos, currentWindowRemainingNanos);
                } else {
                    remainingNanos = currentWindowRemainingNanos;
                }
                return Decision.fromNanos(false, Math.max(remainingNanos, 0L));
            }
        }

        private static final class Window {

            long previousCount;
            long currentCount;
            long startNanos;

            public Window(long startNanos) {
                this.startNanos = startNanos;
            }

        }

    }

}
