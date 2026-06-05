package io.github.tech0ver.ratelimiter.bucket;

import io.github.tech0ver.ratelimiter.ThreadParker;
import io.github.tech0ver.ratelimiter.TimeSource;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

public class ThreadUnsafeBlockingLeakyBucket extends BaseBlockingLeakyBucket {

    // State O(1)
    private long nextFreeTimeNanos;

    public ThreadUnsafeBlockingLeakyBucket(BlockingBucketConfig config) {
        this(config.capacity(), config.interval(), config.timeSource(), config.threadParker());
    }

    public ThreadUnsafeBlockingLeakyBucket(long capacity, Duration interval) {
        this(capacity, interval, TimeSource.MONOTONIC, ThreadParker.DEFAULT);
    }

    public ThreadUnsafeBlockingLeakyBucket(long capacity, Duration interval, TimeSource timeSource, ThreadParker threadParker) {
        super(capacity, interval, timeSource, threadParker);
        this.nextFreeTimeNanos = timeSource.currentTimeNanos();
    }

    @Override
    protected Reservation reserveFreeTime(long maxWaitNanos) {
        long nowNanos = timeSource.currentTimeNanos();
        long earliestFreeTimeNanos = nextFreeTimeNanos - capacityNanos;
        if (earliestFreeTimeNanos >= nowNanos) { // overflow
            long waitNanos = earliestFreeTimeNanos - nowNanos;
            if (waitNanos == 0L) waitNanos = 1L;
            return new Reservation.Denied(waitNanos);
        }
        long freeTimeNanos = Math.max(nextFreeTimeNanos, nowNanos);
        long waitNanos = Math.max(freeTimeNanos - nowNanos, 0L);
        if (0L <= maxWaitNanos && maxWaitNanos < waitNanos) { // overtime
            return new Reservation.Denied(waitNanos);
        }
        nextFreeTimeNanos = freeTimeNanos + nanosPerCount;
        long remainingCount = 0L;
        long oldestCapacityNanos = nowNanos + capacityNanos;
        if (nextFreeTimeNanos <= oldestCapacityNanos) {
            remainingCount = (oldestCapacityNanos - nextFreeTimeNanos) / nanosPerCount;
        }
        return new Reservation.Granted(Math.max(remainingCount, 0L), waitNanos);
    }

}
