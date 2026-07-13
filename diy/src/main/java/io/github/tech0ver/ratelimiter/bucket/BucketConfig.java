package io.github.tech0ver.ratelimiter.bucket;

import io.github.tech0ver.ratelimiter.TimeSource;

import java.time.Duration;

public record BucketConfig(
        long capacity,
        long count,
        Duration interval,
        TimeSource timeSource
) {
}
