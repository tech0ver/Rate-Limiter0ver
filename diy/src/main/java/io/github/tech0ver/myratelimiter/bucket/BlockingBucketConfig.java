package io.github.tech0ver.myratelimiter.bucket;

import io.github.tech0ver.myratelimiter.ThreadParker;
import io.github.tech0ver.myratelimiter.TimeSource;

import java.time.Duration;

public record BlockingBucketConfig(
        long capacity,
        Duration interval,
        TimeSource timeSource,
        ThreadParker threadParker
) {
}
