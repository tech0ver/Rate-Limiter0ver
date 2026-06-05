package io.github.tech0ver.ratelimiter.window;

import io.github.tech0ver.ratelimiter.TimeSource;
import io.github.tech0ver.ratelimiter.window.BaseWindowRateLimiter;
import io.github.tech0ver.ratelimiter.window.WindowConfig;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

public class LockFreeFixedWindowCounter extends BaseWindowRateLimiter {

    // State O(1)
    private record State(long windowStartNanos, long count) {}
    private final AtomicReference<State> stateRef;

    public LockFreeFixedWindowCounter(WindowConfig config) {
        this(config.limit(), config.windowSize(), config.timeSource());
    }

    public LockFreeFixedWindowCounter(long limit, Duration windowSize) {
        this(limit, windowSize, TimeSource.MONOTONIC);
    }

    public LockFreeFixedWindowCounter(long limit, Duration windowSize, TimeSource timeSource) {
        super(limit, windowSize, timeSource);
        this.stateRef = new AtomicReference<>(new State(timeSource.currentTimeNanos(), 0L));
    }

    // O(1)
    @Override
    public Decision tryPermit() {
        while (true) {
            State prev = stateRef.get();
            long nowNanos = timeSource.currentTimeNanos();
            long elapsedNanos = nowNanos - prev.windowStartNanos;
            if (elapsedNanos >= windowSizeNanos) {
                State next = new State(nowNanos, 1L);
                if (stateRef.compareAndSet(prev, next)) {
                    return Decision.asGranted(limit - next.count);
                }
                continue;
            }
            if (prev.count < limit) {
                State next = new State(prev.windowStartNanos, prev.count + 1);
                if (stateRef.compareAndSet(prev, next)) {
                    return Decision.asGranted(limit - next.count);
                }
                continue;
            }
            long remainingNanos = windowSizeNanos - elapsedNanos;
            return Decision.asDenied(Math.max(remainingNanos, 0L));
        }
    }

}
