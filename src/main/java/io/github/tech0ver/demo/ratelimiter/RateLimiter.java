package io.github.tech0ver.demo.ratelimiter;

import java.time.Duration;
import java.util.Optional;

public interface RateLimiter {

    Decision tryPermit();

    interface Decision {

        boolean granted();

        long remaining();

        default boolean denied() {
            return !granted();
        }

        Optional<Duration> retryAfter();

    }

}
