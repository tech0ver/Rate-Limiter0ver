package io.github.tech0ver.ratelimiter;

import java.util.concurrent.TimeUnit;

public interface MyBlockingRateLimiter {

    void acquire(String resource) throws InterruptedException;

    boolean tryAcquire(String resource, long timeout, TimeUnit unit) throws InterruptedException;

}