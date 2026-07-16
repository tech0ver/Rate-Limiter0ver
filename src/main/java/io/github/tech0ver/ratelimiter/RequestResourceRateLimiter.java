package io.github.tech0ver.demo.ratelimiter;

import io.github.tech0ver.demo.ratelimiter.factory.RateLimitedResourceFactory;
import io.github.tech0ver.demo.ratelimiter.factory.RateLimiterFactory;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class RequestResourceRateLimiter implements ResourceRateLimiter<RateLimitedRequest, String> {

    private final RateLimitRule rule;
    private final RateLimiterFactory limiterFactory;
    private final RateLimitedResourceFactory<RateLimitedRequest> resourceFactory;

    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    @Override
    public RateLimitRule rule() {
        return rule;
    }

    @Override
    public Decision<String> tryPermit(RateLimitedRequest request) {
        String resource = resourceFactory.createResourceByRule(rule, request);
        RateLimiter rateLimiter = limiters.computeIfAbsent(resource, k -> limiterFactory.createLimiterByRule(rule));
        RateLimiter.Decision decision = rateLimiter.tryPermit();
        return new DecisionAdapter(rule, resource, decision);
    }

    private record DecisionAdapter(RateLimitRule rule, String resource, RateLimiter.Decision delegate)
            implements Decision<String> {

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
