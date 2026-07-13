package io.github.tech0ver.ratelimiter.window.counter.sliding;

import io.github.tech0ver.ratelimiter.TimeSource;
import io.github.tech0ver.ratelimiter.window.BucketedWindowConfig;

import java.time.Duration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronizedSlidingWindowCounterOnBuckets extends ThreadUnsafeSlidingWindowCounterOnBuckets {

    private final Lock lock;

    public SynchronizedSlidingWindowCounterOnBuckets(BucketedWindowConfig config) {
        this(config.limit(), config.windowSize(), config.numberOfBuckets(), config.timeSource());
    }

    public SynchronizedSlidingWindowCounterOnBuckets(long limit, Duration windowSize, int numberOfBuckets) {
        this(limit, windowSize, numberOfBuckets, TimeSource.MONOTONIC);
    }

    public SynchronizedSlidingWindowCounterOnBuckets(long limit, Duration windowSize, int numberOfBuckets, TimeSource timeSource) {
        super(limit, windowSize, numberOfBuckets, timeSource);
        this.lock = new ReentrantLock();
    }

    @Override
    public Decision tryPermit() {
        lock.lock();
        try {
            return super.tryPermit();
        } finally {
            lock.unlock();
        }
    }

}
