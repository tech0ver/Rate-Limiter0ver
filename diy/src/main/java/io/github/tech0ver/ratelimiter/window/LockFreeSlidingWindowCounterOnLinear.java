package io.github.tech0ver.ratelimiter.window;

import io.github.tech0ver.ratelimiter.Requires;
import io.github.tech0ver.ratelimiter.TimeSource;
import io.github.tech0ver.ratelimiter.window.BaseWindowRateLimiter;
import io.github.tech0ver.ratelimiter.window.WindowConfig;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

public class LockFreeSlidingWindowCounterOnLinear extends BaseWindowRateLimiter {

    // State O(1)
    private record State(long windowStartNanos, long previousCount, long currentCount) {}
    private final AtomicReference<State> stateRef;

    public LockFreeSlidingWindowCounterOnLinear(WindowConfig config) {
        this(config.limit(), config.windowSize(), config.timeSource());
    }

    public LockFreeSlidingWindowCounterOnLinear(long limit, Duration windowSize, TimeSource timeSource) {
        super(limit, windowSize, timeSource);
        long maxWindowSizeNanos = Long.MAX_VALUE / 2L;
        long windowSizeNanos = Requires.between(windowSize.toNanos(), 1L, maxWindowSizeNanos,
                "windowSize must be between 1.." + maxWindowSizeNanos + ": " + windowSize);
        long maxLimit = Long.MAX_VALUE / (2L * windowSizeNanos);
        Requires.between(limit, 1L, maxLimit,
                "limit for given windowSize must be between 1.." + maxLimit + ": " + limit);
        this.stateRef = new AtomicReference<>(new State(timeSource.currentTimeNanos(), 0L, 0L));
    }

    // O(1)
    @Override
    public Decision tryPermit() {
        while (true) {
            State prev = stateRef.get();
            long nowNanos = timeSource.currentTimeNanos();
            long currentWindowStartNanos = nowNanos - (nowNanos % windowSizeNanos);
            if (prev.windowStartNanos < currentWindowStartNanos) {
                long windowsPassed = (currentWindowStartNanos - prev.windowStartNanos) / windowSizeNanos;
                long previousCount = windowsPassed == 1L ? prev.currentCount : 0L;
                State next = new State(currentWindowStartNanos, previousCount, 0L);
                stateRef.compareAndSet(prev, next);
                continue;
            }
            long elapsedNanos = nowNanos - prev.windowStartNanos;
            long remainingNanos = windowSizeNanos - elapsedNanos;
            long slidingNanos = prev.previousCount * remainingNanos + prev.currentCount * windowSizeNanos;
            long limitNanos = limit * windowSizeNanos;
            if (slidingNanos < limitNanos) {
                long currentCount = prev.currentCount + 1L;
                State next = new State(prev.windowStartNanos, prev.previousCount, currentCount);
                if (stateRef.compareAndSet(prev, next)) {
                    slidingNanos = next.previousCount * remainingNanos + next.currentCount * windowSizeNanos;
                    return Decision.asGranted((limitNanos - slidingNanos) / windowSizeNanos);
                }
                continue;
            }
            if (prev.previousCount > 0L) {
                long overflowNanos = slidingNanos - limitNanos;
                long waitNanos = (overflowNanos / prev.previousCount) + 1L;
                remainingNanos = Math.min(remainingNanos, waitNanos);
            }
            return Decision.asDenied(Math.max(remainingNanos, 0L));
        }
    }

}
