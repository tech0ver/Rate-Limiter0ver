package io.github.tech0ver.demo.ratelimiter.adapter;

import io.github.tech0ver.demo.ratelimiter.RateLimiter;
import io.github.tech0ver.ratelimiter.MyRateLimiter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Optional;

@RequiredArgsConstructor
public class DiyRateLimiterAdapter implements RateLimiter {

    private final MyRateLimiter delegate;

    @Override
    public Decision tryPermit() {
        MyRateLimiter.Decision decision = delegate.tryPermit();
        return new DecisionAdapter(decision);
    }

    private record DecisionAdapter(MyRateLimiter.Decision delegate) implements Decision {

        @Override
        public boolean granted() {
            return delegate.granted();
        }

        @Override
        public long remaining() {
            return delegate.remaining();
        }

        @Override
        public Optional<Duration> retryAfter() {
            return delegate.retryAfter();
        }

    }

}
