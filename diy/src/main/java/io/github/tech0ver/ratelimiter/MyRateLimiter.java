package io.github.tech0ver.ratelimiter;

import java.time.Duration;
import java.util.Optional;

public interface MyRateLimiter {

    Decision tryPermit();

    default boolean isGranted() {
        return tryPermit().granted();
    }

    interface Decision {

        static Decision asGranted(long remaining) {
            Requires.positiveOrZero(remaining, "remaining must be >= 0");
            return new Decision() {

                @Override public boolean granted() { return true; }

                @Override public long remaining() { return remaining; }

            };
        }

        static Decision asDenied(long retryAfterNanos) {
            Requires.positiveOrZero(retryAfterNanos, "retryAfterNanos must be >= 0");
            Duration retryAfter = Duration.ofNanos(retryAfterNanos);
            return new Decision() {

                @Override public boolean granted() { return false; }

                @Override public Optional<Duration> retryAfter() { return Optional.of(retryAfter); }

            };
        }

        boolean granted();

        default long remaining() {
            return 0L;
        }

        default boolean denied() {
            return !granted();
        }

        default Optional<Duration> retryAfter() {
            return Optional.empty();
        }

    }

}
