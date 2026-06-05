package io.github.tech0ver.ratelimiter.bucket;

import io.github.tech0ver.ratelimiter.ThreadParker;
import io.github.tech0ver.ratelimiter.TimeSource;

import java.time.Duration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronizedBlockingLeakyBucket extends ThreadUnsafeBlockingLeakyBucket {

    private final Lock lock;

    public SynchronizedBlockingLeakyBucket(BlockingBucketConfig config) {
        this(config.capacity(), config.interval(), config.timeSource(), config.threadParker());
    }

    public SynchronizedBlockingLeakyBucket(long capacity, Duration interval) {
        this(capacity, interval, TimeSource.MONOTONIC, ThreadParker.DEFAULT);
    }

    public SynchronizedBlockingLeakyBucket(long capacity, Duration interval, TimeSource timeSource, ThreadParker threadParker) {
        super(capacity, interval, timeSource, threadParker);
        this.lock = new ReentrantLock();
    }

    @Override
    protected Reservation reserveFreeTime(long maxWaitNanos) {
        lock.lock();
        try {
            return super.reserveFreeTime(maxWaitNanos);
        } finally {
            lock.unlock();
        }
    }

}
