package io.github.tech0ver.ratelimiter.bucket;

import io.github.tech0ver.ratelimiter.ThreadParker;
import io.github.tech0ver.ratelimiter.TimeSource;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

public class LockFreeBlockingLeakyBucket extends BaseBlockingLeakyBucket {

    // State O(1)
    private final AtomicLong nextFreeTimeNanos;

    public LockFreeBlockingLeakyBucket(BlockingBucketConfig config) {
        this(config.capacity(), config.interval(), config.timeSource(), config.threadParker());
    }

    public LockFreeBlockingLeakyBucket(long capacity, Duration interval) {
        this(capacity, interval, TimeSource.MONOTONIC, ThreadParker.DEFAULT);
    }

    public LockFreeBlockingLeakyBucket(long capacity, Duration interval, TimeSource timeSource, ThreadParker threadParker) {
        super(capacity, interval, timeSource, threadParker);
        this.nextFreeTimeNanos = new AtomicLong(timeSource.currentTimeNanos());
    }

    @Override
    protected Reservation reserveFreeTime(long maxWaitNanos) {
        while (true) {
            long nowNanos = timeSource.currentTimeNanos();
            long observedNextFreeTimeNanos = nextFreeTimeNanos.get();
            long earliestFreeTimeNanos = observedNextFreeTimeNanos - capacityNanos;
            if (earliestFreeTimeNanos >= nowNanos) { // overflow
                long waitNanos = earliestFreeTimeNanos - nowNanos;
                if (waitNanos == 0L) waitNanos = 1L;
                return new Reservation.Denied(waitNanos);
            }
            long freeTimeNanos = Math.max(observedNextFreeTimeNanos, nowNanos);
            long waitNanos = Math.max(freeTimeNanos - nowNanos, 0L);
            if (0L <= maxWaitNanos && maxWaitNanos < waitNanos) { // overtime
                return new Reservation.Denied(waitNanos);
            }
            long newNextFreeTimeNanos = freeTimeNanos + nanosPerCount;
            if (nextFreeTimeNanos.compareAndSet(observedNextFreeTimeNanos, newNextFreeTimeNanos)) {
                long remainingCount = 0L;
                long oldestCapacityNanos = nowNanos + capacityNanos;
                if (newNextFreeTimeNanos <= oldestCapacityNanos) {
                    remainingCount = (oldestCapacityNanos - newNextFreeTimeNanos) / nanosPerCount;
                }
                return new Reservation.Granted(Math.max(remainingCount, 0L), waitNanos);
            }
        }
    }

}
