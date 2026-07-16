package io.github.tech0ver.demo.ratelimiter.factory;

import io.github.tech0ver.demo.ratelimiter.RateLimitRule;
import io.github.tech0ver.demo.ratelimiter.RateLimiter;

public interface RateLimiterFactory {

    RateLimiter createLimiterByRule(RateLimitRule rule);

}
