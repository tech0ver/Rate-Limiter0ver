package io.github.tech0ver.ratelimiter;

import java.time.Duration;

public interface MyRateLimiter {

    default boolean isAllowed(String resource) {
        return decide(resource).isAllowed();
    }

    Decision decide(String resource);

    interface Decision {

        static Decision fromNanos(boolean allowed, long retryAfterNanos) {
            if (retryAfterNanos < 0) throw new IllegalArgumentException("retryAfterNanos must be >= 0");
            return new Decision() {

                @Override
                public boolean isAllowed() {
                    return allowed;
                }

                @Override
                public long retryAfterNanos() {
                    return retryAfterNanos;
                }

            };
        }

        boolean isAllowed();

        long retryAfterNanos();

        default Duration retryAfter() {
            return Duration.ofNanos(retryAfterNanos());
        }

    }

}