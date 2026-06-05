package io.github.tech0ver.ratelimiter.window;

import io.github.tech0ver.ratelimiter.Requires;
import io.github.tech0ver.ratelimiter.TimeSource;

import java.time.Duration;

public class ThreadUnsafeSlidingWindowCounterOnBuckets extends BaseWindowRateLimiter {

    // Configuration O(1)
    private final int numberOfBuckets;
    private final long bucketSizeNanos;

    // State O(numberOfBuckets)
    private static class Bucket { long startNanos, count; }
    private final Bucket[] buckets;

    public ThreadUnsafeSlidingWindowCounterOnBuckets(BucketedWindowConfig config) {
        this(config.limit(), config.windowSize(), config.numberOfBuckets(), config.timeSource());
    }

    public ThreadUnsafeSlidingWindowCounterOnBuckets(long limit, Duration windowSize, int numberOfBuckets) {
        this(limit, windowSize, numberOfBuckets, TimeSource.MONOTONIC);
    }

    public ThreadUnsafeSlidingWindowCounterOnBuckets(long limit, Duration windowSize, int numberOfBuckets, TimeSource timeSource) {
        super(limit, windowSize, timeSource);
        this.numberOfBuckets = Requires.positive(numberOfBuckets, "numberOfBuckets must be > 0");
        this.bucketSizeNanos = windowSize.toNanos() / numberOfBuckets;
        this.buckets = new Bucket[numberOfBuckets];
        long nowNanos = timeSource.currentTimeNanos();
        long bucketStartNanos = nowNanos - (nowNanos % bucketSizeNanos);
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = new Bucket();
            buckets[i].startNanos = bucketStartNanos;
        }
    }

    // O(numberOfBuckets)
    @Override
    public Decision tryPermit() {
        long nowNanos = timeSource.currentTimeNanos();
        int currentBucketIndex = (int) ((nowNanos / bucketSizeNanos) % numberOfBuckets);
        long currentBucketStartNanos = nowNanos - (nowNanos % bucketSizeNanos);
        if (nowNanos - buckets[currentBucketIndex].startNanos >= bucketSizeNanos) {
            buckets[currentBucketIndex].startNanos = currentBucketStartNanos;
            buckets[currentBucketIndex].count = 0L;
        }
        long totalCount = 0;
        int oldestNotEmptyBucketIndex = currentBucketIndex;
        for (int i = 0; i < numberOfBuckets; i++) {
            if (nowNanos - buckets[i].startNanos < windowSizeNanos && buckets[i].count > 0L) {
                totalCount += buckets[i].count;
                if (buckets[oldestNotEmptyBucketIndex].startNanos > buckets[i].startNanos) {
                    oldestNotEmptyBucketIndex = i;
                }
            }
        }
        if (totalCount < limit) {
            buckets[currentBucketIndex].count++;
            return Decision.asGranted(limit - totalCount - 1L);
        }
        long elapsedNanos = nowNanos - buckets[oldestNotEmptyBucketIndex].startNanos;
        long remainingNanos = windowSizeNanos - elapsedNanos;
        return Decision.asDenied(Math.max(remainingNanos, 0L));
    }

}
