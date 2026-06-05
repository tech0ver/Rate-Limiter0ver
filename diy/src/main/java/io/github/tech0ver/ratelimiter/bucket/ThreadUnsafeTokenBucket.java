package io.github.tech0ver.ratelimiter.bucket;

import io.github.tech0ver.ratelimiter.TimeSource;

import java.time.Duration;

public class ThreadUnsafeTokenBucket extends BaseBucketRateLimiter {

    // State O(1)
    private long lastRefillNanos;
    private double count;

    public ThreadUnsafeTokenBucket(BucketConfig config) {
        this(config.capacity(), config.count(), config.interval(), config.timeSource());
    }

    public ThreadUnsafeTokenBucket(long capacity, Duration interval) {
        this(capacity, interval, TimeSource.MONOTONIC);
    }

    public ThreadUnsafeTokenBucket(long capacity, Duration interval, TimeSource timeSource) {
        this(capacity, 1, interval, timeSource);
    }

    public ThreadUnsafeTokenBucket(long capacity, long count, Duration interval, TimeSource timeSource) {
        super(capacity, count, interval, timeSource);
        this.lastRefillNanos = timeSource.currentTimeNanos();
        this.count = capacity;
    }

    // O(1)
    @Override
    public Decision tryPermit() {
        long nowNanos = timeSource.currentTimeNanos();
        if (lastRefillNanos < nowNanos) {
            long elapsedSinceLastRefillNanos = nowNanos - lastRefillNanos;
            lastRefillNanos = nowNanos;
            double refilledCount = elapsedSinceLastRefillNanos * countPerNano;
            count = Math.min(count + refilledCount, capacity);
        }
        if (count < 1.0) {
            double missingCount = 1.0 - count;
            long remainingNanos = (long) Math.ceil(missingCount / countPerNano);
            return Decision.asDenied(Math.max(remainingNanos, 0L));
        }
        count -= 1.0;
        return Decision.asGranted((long) count);
    }

}
