package io.github.tech0ver.ratelimiter.window;

import io.github.tech0ver.ratelimiter.TimeSource;

import java.time.Duration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronizedSlidingWindowCounterOnLinear extends ThreadUnsafeSlidingWindowCounterOnLinear {

    private final Lock lock;

    public SynchronizedSlidingWindowCounterOnLinear(WindowConfig config) {
        this(config.limit(), config.windowSize(), config.timeSource());
    }

    public SynchronizedSlidingWindowCounterOnLinear(long limit, Duration windowSize) {
        this(limit, windowSize, TimeSource.MONOTONIC);
    }

    public SynchronizedSlidingWindowCounterOnLinear(long limit, Duration windowSize, TimeSource timeSource) {
        super(limit, windowSize, timeSource);
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
