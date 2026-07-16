package io.github.tech0ver.demo.ratelimiter.adapter;

import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.local.LocalBucket;
import io.github.tech0ver.demo.ratelimiter.RateLimiter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Optional;

@RequiredArgsConstructor
public class Bucket4JRateLimiterAdapter implements RateLimiter {

    private final LocalBucket delegate;

    @Override
    public Decision tryPermit() {
        ConsumptionProbe probe = delegate.tryConsumeAndReturnRemaining(1L);
        return new DecisionAdapter(probe);
    }

    private record DecisionAdapter(ConsumptionProbe delegate) implements Decision {

        @Override
        public boolean granted() {
            return delegate.isConsumed();
        }

        @Override
        public long remaining() {
            return delegate.getRemainingTokens();
        }

        @Override
        public Optional<Duration> retryAfter() {
            if (delegate.isConsumed()) return Optional.empty();
            Duration retryAfter = Duration.ofNanos(delegate.getNanosToWaitForRefill());
            return Optional.of(retryAfter);
        }

    }

}
