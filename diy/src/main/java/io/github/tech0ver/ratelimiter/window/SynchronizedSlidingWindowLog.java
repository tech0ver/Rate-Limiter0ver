package io.github.tech0ver.ratelimiter.window;

import io.github.tech0ver.ratelimiter.Requires;
import io.github.tech0ver.ratelimiter.TimeSource;

import java.time.Duration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronizedSlidingWindowLog extends ThreadUnsafeSlidingWindowLog {

    private final Lock lock;

    public SynchronizedSlidingWindowLog(WindowConfig config) {
        this((int) Requires.between(config.limit(), 1L, Integer.MAX_VALUE,
                        "limit must be between 1.." + Integer.MAX_VALUE),
                config.windowSize(), config.timeSource());
    }

    public SynchronizedSlidingWindowLog(int limit, Duration windowSize) {
        this(limit, windowSize, TimeSource.MONOTONIC);
    }

    public SynchronizedSlidingWindowLog(int limit, Duration windowSize, TimeSource timeSource) {
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
