package io.github.tech0ver.ratelimiter.window;

import io.github.tech0ver.ratelimiter.TimeSource;

import java.time.Duration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronizedFixedWindowCounter extends ThreadUnsafeFixedWindowCounter {

    private final Lock lock;

    public SynchronizedFixedWindowCounter(WindowConfig config) {
        this(config.limit(), config.windowSize(), config.timeSource());
    }

    public SynchronizedFixedWindowCounter(long limit, Duration windowSize) {
        this(limit, windowSize, TimeSource.MONOTONIC);
    }

    public SynchronizedFixedWindowCounter(long limit, Duration windowSize, TimeSource timeSource) {
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
