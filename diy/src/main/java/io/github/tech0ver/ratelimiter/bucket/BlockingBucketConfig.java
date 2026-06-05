package io.github.tech0ver.ratelimiter.bucket;

import io.github.tech0ver.ratelimiter.Requires;
import io.github.tech0ver.ratelimiter.ThreadParker;
import io.github.tech0ver.ratelimiter.TimeSource;

import java.time.Duration;

public record BlockingBucketConfig(
        long capacity,
        Duration interval,
        TimeSource timeSource,
        ThreadParker threadParker
) {
}
