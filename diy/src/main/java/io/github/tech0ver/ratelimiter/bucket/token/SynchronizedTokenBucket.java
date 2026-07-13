package io.github.tech0ver.ratelimiter.bucket.token;

import io.github.tech0ver.ratelimiter.MyRateLimiter;
import io.github.tech0ver.ratelimiter.TimeSource;
import io.github.tech0ver.ratelimiter.bucket.BucketConfig;

import java.time.Duration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronizedTokenBucket extends ThreadUnsafeTokenBucket {

    private final Lock lock;

    public SynchronizedTokenBucket(BucketConfig config) {
        this(config.capacity(), config.count(), config.interval(), config.timeSource());
    }

    public SynchronizedTokenBucket(long capacity, Duration interval) {
        this(capacity, interval, TimeSource.MONOTONIC);
    }

    public SynchronizedTokenBucket(long capacity, Duration interval, TimeSource timeSource) {
        this(capacity, 1, interval, timeSource);
    }

    public SynchronizedTokenBucket(long capacity, long count, Duration interval, TimeSource timeSource) {
        super(capacity, count, interval, timeSource);
        this.lock = new ReentrantLock();
    }

    @Override
    public MyRateLimiter.Decision tryPermit() {
        lock.lock();
        try {
            return super.tryPermit();
        } finally {
            lock.unlock();
        }
    }

}
