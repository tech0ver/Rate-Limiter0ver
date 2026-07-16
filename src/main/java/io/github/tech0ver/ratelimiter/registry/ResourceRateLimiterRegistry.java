package io.github.tech0ver.ratelimiter.registry;

import io.github.tech0ver.ratelimiter.ResourceRateLimiter;

import java.util.Optional;

public interface ResourceRateLimiterRegistry<T, R> {

    void register(ResourceRateLimiter<T, R> rateLimiter);

    Optional<ResourceRateLimiter.Decision<R>> tryPermit(T resource);

}
