package io.github.tech0ver.ratelimiter.window;

import io.github.tech0ver.ratelimiter.TimeSource;
import io.github.tech0ver.ratelimiter.window.BaseWindowRateLimiter;
import io.github.tech0ver.ratelimiter.window.WindowConfig;

import java.time.Duration;

public class ThreadUnsafeFixedWindowCounter extends BaseWindowRateLimiter {

    // State O(1)
    private long windowStartNanos;
    private long count;

    public ThreadUnsafeFixedWindowCounter(WindowConfig config) {
        this(config.limit(), config.windowSize(), config.timeSource());
    }

    public ThreadUnsafeFixedWindowCounter(long limit, Duration windowSize) {
        this(limit, windowSize, TimeSource.MONOTONIC);
    }

    public ThreadUnsafeFixedWindowCounter(long limit, Duration windowSize, TimeSource timeSource) {
        super(limit, windowSize, timeSource);
        this.windowStartNanos = timeSource.currentTimeNanos();
        this.count = 0L;
    }

    // O(1)
    @Override
    public Decision tryPermit() {
        long nowNanos = timeSource.currentTimeNanos();
        long elapsedNanos = nowNanos - windowStartNanos;
        if (elapsedNanos >= windowSizeNanos) {
            windowStartNanos = nowNanos;
            count = 1L;
            return Decision.asGranted(limit - count);
        }
        if (count < limit) {
            count++;
            return Decision.asGranted(limit - count);
        }
        long remainingNanos = windowSizeNanos - elapsedNanos;
        return Decision.asDenied(Math.max(remainingNanos, 0L));
    }

}
