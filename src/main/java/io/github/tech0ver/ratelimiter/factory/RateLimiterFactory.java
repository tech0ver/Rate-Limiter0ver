package io.github.tech0ver.ratelimiter.factory;

import io.github.tech0ver.ratelimiter.RateLimitRule;
import io.github.tech0ver.ratelimiter.RateLimiter;

public interface RateLimiterFactory {

    RateLimiter createLimiterByRule(RateLimitRule rule);

}
