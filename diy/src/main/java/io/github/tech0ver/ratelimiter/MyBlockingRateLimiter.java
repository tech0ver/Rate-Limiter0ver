package io.github.tech0ver.ratelimiter;

import java.time.Duration;
import java.util.Optional;

public interface MyBlockingRateLimiter extends MyRateLimiter {

    long permit();

    long permitInterruptibly() throws InterruptedException;

    @Override
    BlockingDecision tryPermit();

    BlockingDecision tryPermitInterruptibly() throws InterruptedException;

    BlockingDecision tryPermit(Duration waitTime);

    BlockingDecision tryPermitInterruptibly(Duration waitTime) throws InterruptedException;;

    interface BlockingDecision extends Decision {

        static BlockingDecision asGranted(long remaining, long waitedBeforeNanos) {
            Requires.positiveOrZero(remaining, "remaining must be >= 0");
            Requires.positiveOrZero(waitedBeforeNanos, "waitedBeforeNanos must be >= 0");
            Duration waitBefore = Duration.ofNanos(waitedBeforeNanos);
            return new BlockingDecision() {

                @Override public boolean granted() {
                    return true;
                }

                @Override public long remaining() { return remaining; }

                @Override public Optional<Duration> waitedBefore() { return Optional.of(waitBefore); }

            };
        }

        static BlockingDecision asDenied(long retryAfterNanos) {
            Requires.positiveOrZero(retryAfterNanos, "retryAfterNanos must be >= 0");
            Duration retryAfter = Duration.ofNanos(retryAfterNanos);
            return new BlockingDecision() {

                @Override public boolean granted() {
                    return false;
                }

                @Override public Optional<Duration> retryAfter() { return Optional.of(retryAfter); }

            };
        }

        default Optional<Duration> waitedBefore() {
            return Optional.empty();
        }

    }

}
