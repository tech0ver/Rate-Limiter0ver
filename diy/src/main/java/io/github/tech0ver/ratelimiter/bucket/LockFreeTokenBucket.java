package io.github.tech0ver.ratelimiter.bucket;

import io.github.tech0ver.ratelimiter.TimeSource;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

public class LockFreeTokenBucket extends BaseBucketRateLimiter {

    // State O(1)
    private record State(long lastRefillNanos, double count) {}
    private final AtomicReference<State> stateRef;

    public LockFreeTokenBucket(BucketConfig config) {
        this(config.capacity(), config.count(), config.interval(), config.timeSource());
    }

    public LockFreeTokenBucket(long capacity, Duration interval) {
        this(capacity, interval, TimeSource.MONOTONIC);
    }

    public LockFreeTokenBucket(long capacity, Duration interval, TimeSource timeSource) {
        this(capacity, 1, interval, timeSource);
    }

    public LockFreeTokenBucket(long capacity, long count, Duration interval, TimeSource timeSource) {
        super(capacity, count, interval, timeSource);
        this.stateRef = new AtomicReference<>(new State(timeSource.currentTimeNanos(), capacity));
    }

    // O(1)
    @Override
    public Decision tryPermit() {
        while (true) {
            State prev = stateRef.get();
            long nowNanos = timeSource.currentTimeNanos();
            if (prev.lastRefillNanos < nowNanos) {
                long elapsedSinceLastRefillNanos = nowNanos - prev.lastRefillNanos;
                double refilledCount = elapsedSinceLastRefillNanos * countPerNano;
                State next = new State(nowNanos, Math.min(prev.count + refilledCount, capacity));
                if (!stateRef.compareAndSet(prev, next)) {
                    continue;
                }
                prev = stateRef.get();
            }
            if (prev.count < 1.0) {
                double missingCount = 1.0 - prev.count;
                long remainingNanos = (long) Math.ceil(missingCount / countPerNano);
                return Decision.asDenied(Math.max(remainingNanos, 0L));
            }
            State next = new State(prev.lastRefillNanos, prev.count - 1.0);
            if (stateRef.compareAndSet(prev, next)) {
                return Decision.asGranted((long) next.count);
            }
        }
    }

}
