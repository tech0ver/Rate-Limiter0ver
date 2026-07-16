package io.github.tech0ver.demo.ratelimiter.factory;

import io.github.tech0ver.demo.ratelimiter.RateLimitRule;

public interface RateLimitedResourceFactory<T> {

    String createResourceByRule(RateLimitRule rule, T payload);

}
