package io.github.tech0ver.ratelimiter.bucket;

import io.github.tech0ver.ratelimiter.TimeSource;

import java.time.Duration;

public class ThreadUnsafeLeakyBucket extends BaseBucketRateLimiter {

    // State O(1)
    private long lastUpdateNanos;
    private double count;

    public ThreadUnsafeLeakyBucket(BucketConfig config) {
        this(config.capacity(), config.count(), config.interval(), config.timeSource());
    }

    public ThreadUnsafeLeakyBucket(long capacity, Duration interval) {
        this(capacity, interval, TimeSource.MONOTONIC);
    }

    public ThreadUnsafeLeakyBucket(long capacity, Duration interval, TimeSource timeSource) {
        this(capacity, 1L, interval, timeSource);
    }

    public ThreadUnsafeLeakyBucket(long capacity, long count, Duration interval, TimeSource timeSource) {
        super(capacity, count, interval, timeSource);
        this.lastUpdateNanos = timeSource.currentTimeNanos();
        this.count = 0.0;
    }

    // O(1)
    @Override
    public Decision tryPermit() {
        long nowNanos = timeSource.currentTimeNanos();
        if (lastUpdateNanos < nowNanos) {
            long elapsedSinceLastUpdateNanos = nowNanos - lastUpdateNanos;
            lastUpdateNanos = nowNanos;
            double leakedCount = elapsedSinceLastUpdateNanos * countPerNano;
            count = Math.max(count - leakedCount, 0.0);
        }
        if (count + 1.0 > capacity) {
            double overflowCount = count + 1.0 - capacity;
            long remainingNanos = (long) Math.ceil(overflowCount / countPerNano);
            return Decision.asDenied(Math.max(remainingNanos, 0L));
        }
        count += 1.0;
        return Decision.asGranted(capacity - (long) count);
    }

}
