package io.github.tech0ver.ratelimiter.bucket;

import io.github.tech0ver.ratelimiter.MyRateLimiter;
import io.github.tech0ver.ratelimiter.Requires;
import io.github.tech0ver.ratelimiter.TimeSource;

import java.time.Duration;

public abstract class BaseBucketRateLimiter implements MyRateLimiter {

    // Configuration O(1)
    protected final long capacity;
    protected final double countPerNano;
    protected final TimeSource timeSource;

    protected BaseBucketRateLimiter(long capacity, long count, Duration interval, TimeSource timeSource) {
        this.capacity = Requires.positive(capacity, "capacity must be > 0");
        double intervalNanos = Requires.positive(interval, "interval must be > 0").toNanos();
        this.countPerNano =  Requires.positive(count, "count must be > 0") / intervalNanos;
        this.timeSource = Requires.nonNull(timeSource, "No timeSource");
    }

}
