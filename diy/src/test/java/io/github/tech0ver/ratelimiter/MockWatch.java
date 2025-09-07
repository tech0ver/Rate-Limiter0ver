package io.github.tech0ver.ratelimiter;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

public class MockWatch implements MyWatch {

    private final AtomicLong timeNanos;

    public MockWatch() {
        timeNanos = new AtomicLong(0);
    }

    public void advance(long deltaNanos) {
        timeNanos.addAndGet(deltaNanos);
    }

    public void advance(Duration delta) {
        advance(delta.toNanos());
    }

    @Override
    public long currentTimeNanos() {
        return timeNanos.get();
    }

}
