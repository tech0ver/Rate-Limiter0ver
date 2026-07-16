package io.github.tech0ver.ratelimiter.factory;

import io.github.tech0ver.ratelimiter.RateLimitRule;

public interface RateLimitedResourceFactory<T> {

    String createResourceByRule(RateLimitRule rule, T payload);

}
