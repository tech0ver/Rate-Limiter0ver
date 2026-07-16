package io.github.tech0ver.myratelimiter.bucket;

import io.github.tech0ver.myratelimiter.TimeSource;

import java.time.Duration;

public record BucketConfig(
        long capacity,
        long count,
        Duration interval,
        TimeSource timeSource
) {
}
