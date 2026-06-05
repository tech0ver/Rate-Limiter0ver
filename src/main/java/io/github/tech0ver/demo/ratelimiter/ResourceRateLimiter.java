package io.github.tech0ver.demo.ratelimiter;

public interface ResourceRateLimiter<T, R> {

    RateLimitRule rule();

    Decision<R> tryPermit(T resource);

    interface Decision<R> extends RateLimiter.Decision {

        RateLimitRule rule();

        R resource();

    }

}
