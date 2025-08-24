package io.github.tech0ver.ratelimiter;

public interface MyRateLimiter {

    boolean isAllowed(String resource);

}