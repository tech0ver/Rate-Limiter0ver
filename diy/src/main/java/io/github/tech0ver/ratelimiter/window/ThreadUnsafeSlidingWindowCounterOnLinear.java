package io.github.tech0ver.ratelimiter.window;

import io.github.tech0ver.ratelimiter.Requires;
import io.github.tech0ver.ratelimiter.TimeSource;
import io.github.tech0ver.ratelimiter.window.BaseWindowRateLimiter;
import io.github.tech0ver.ratelimiter.window.WindowConfig;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

public class ThreadUnsafeSlidingWindowCounterOnLinear extends BaseWindowRateLimiter {

    // State O(1)
    private long windowStartNanos;
    private long previousCount;
    private long currentCount;

    public ThreadUnsafeSlidingWindowCounterOnLinear(WindowConfig config) {
        this(config.limit(), config.windowSize(), config.timeSource());
    }

    public ThreadUnsafeSlidingWindowCounterOnLinear(long limit, Duration windowSize) {
        this(limit, windowSize, TimeSource.MONOTONIC);
    }

    public ThreadUnsafeSlidingWindowCounterOnLinear(long limit, Duration windowSize, TimeSource timeSource) {
        super(limit, windowSize, timeSource);
        long maxWindowSizeNanos = Long.MAX_VALUE / 2L;
        long windowSizeNanos = Requires.between(windowSize.toNanos(), 1L, maxWindowSizeNanos,
                "windowSize must be between 1.." + maxWindowSizeNanos + ": " + windowSize);
        long maxLimit = Long.MAX_VALUE / (2L * windowSizeNanos);
        Requires.between(limit, 1L, maxLimit,
                "limit for given windowSize must be between 1.." + maxLimit + ": " + limit);
    }

    // O(1)
    @Override
    public Decision tryPermit() {
        long nowNanos = timeSource.currentTimeNanos();
        long currentWindowStartNanos = nowNanos - (nowNanos % windowSizeNanos);
        if (windowStartNanos < currentWindowStartNanos) {
            long windowsPassed = (currentWindowStartNanos - windowStartNanos) / windowSizeNanos;
            windowStartNanos = currentWindowStartNanos;
            previousCount = windowsPassed == 1L ? currentCount : 0L;
            currentCount = 0L;
        }
        long elapsedNanos = nowNanos - windowStartNanos;
        long remainingNanos = windowSizeNanos - elapsedNanos;
        long slidingNanos = previousCount * remainingNanos + currentCount * windowSizeNanos;
        long limitNanos = limit * windowSizeNanos;
        if (slidingNanos < limitNanos) {
            currentCount++;
            slidingNanos = previousCount * remainingNanos + currentCount * windowSizeNanos;
            return Decision.asGranted((limitNanos - slidingNanos) / windowSizeNanos);
        }
        if (previousCount > 0L) {
            long overflowNanos = slidingNanos - limitNanos;
            long waitNanos = (overflowNanos / previousCount) + 1L;
            remainingNanos = Math.min(remainingNanos, waitNanos);
        }
        return Decision.asDenied(Math.max(remainingNanos, 0L));
    }

}
