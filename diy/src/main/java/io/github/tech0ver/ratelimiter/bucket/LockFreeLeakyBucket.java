package io.github.tech0ver.ratelimiter.bucket;

import io.github.tech0ver.ratelimiter.TimeSource;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

public class LockFreeLeakyBucket extends BaseBucketRateLimiter {

    // State O(1)
    private record State(long lastUpdateNanos, double count) {}
    private final AtomicReference<State> stateRef;

    public LockFreeLeakyBucket(BucketConfig config) {
        this(config.capacity(), config.count(), config.interval(), config.timeSource());
    }

    public LockFreeLeakyBucket(long capacity, Duration interval) {
        this(capacity, interval, TimeSource.MONOTONIC);
    }

    public LockFreeLeakyBucket(long capacity, Duration interval, TimeSource timeSource) {
        this(capacity, 1L, interval, timeSource);
    }

    public LockFreeLeakyBucket(long capacity, long count, Duration interval, TimeSource timeSource) {
        super(capacity, count, interval, timeSource);
        this.stateRef = new AtomicReference<>(new State(timeSource.currentTimeNanos(), 0.0));
    }

    // O(1)
    @Override
    public Decision tryPermit() {
        while (true) {
            State prev = stateRef.get();
            long nowNanos = timeSource.currentTimeNanos();
            if (prev.lastUpdateNanos < nowNanos) {
                long elapsedSinceLastUpdateNanos = nowNanos - prev.lastUpdateNanos;
                double leakedCount = elapsedSinceLastUpdateNanos * countPerNano;
                State next = new State(nowNanos, Math.max(prev.count - leakedCount, 0.0));
                if (!stateRef.compareAndSet(prev, next)) {
                    continue;
                }
                prev = stateRef.get();
            }
            if (prev.count + 1.0 > capacity) {
                double overflowCount = prev.count + 1.0 - capacity;
                long remainingNanos = (long) Math.ceil(overflowCount / countPerNano);
                return Decision.asDenied(Math.max(remainingNanos, 0L));
            }
            State next = new State(prev.lastUpdateNanos, prev.count + 1.0);
            if (stateRef.compareAndSet(prev, next)) {
                return Decision.asGranted(capacity - (long) next.count);
            }
        }
    }

}
