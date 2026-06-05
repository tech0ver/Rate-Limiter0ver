package io.github.tech0ver.ratelimiter;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

public class MockedTimeSource implements TimeSource {

    private final AtomicLong timeNanos;

    public MockedTimeSource() {
        timeNanos = new AtomicLong();
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
