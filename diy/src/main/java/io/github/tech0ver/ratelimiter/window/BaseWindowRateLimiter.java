package io.github.tech0ver.ratelimiter.window;

import io.github.tech0ver.ratelimiter.MyRateLimiter;
import io.github.tech0ver.ratelimiter.Requires;
import io.github.tech0ver.ratelimiter.TimeSource;

import java.time.Duration;

public abstract class BaseWindowRateLimiter implements MyRateLimiter {

    // Configuration O(1)
    protected final long limit;
    protected final long windowSizeNanos;
    protected final TimeSource timeSource;

    protected BaseWindowRateLimiter(long limit, Duration windowSize, TimeSource timeSource) {
        this.limit = Requires.positive(limit, "limit must be > 0");
        this.windowSizeNanos = Requires.positive(windowSize, "windowSize must be > 0").toNanos();
        this.timeSource = Requires.nonNull(timeSource, "No timeSource");
    }

    @Override
    public abstract Decision tryPermit();

}
