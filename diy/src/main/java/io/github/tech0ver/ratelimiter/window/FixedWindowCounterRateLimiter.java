package io.github.tech0ver.ratelimiter.window;

import io.github.tech0ver.ratelimiter.MyRateLimiter;
import io.github.tech0ver.ratelimiter.MyWatch;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class FixedWindowCounterRateLimiter implements MyRateLimiter {

    // O(N) where N is the number of active resources in the current window
    private final Map<String, AtomicLong> counterByResource;
    private final MyWatch watch;
    private final long limit;
    private final long windowSizeNanos;
    private volatile long windowStartNanos;

    public FixedWindowCounterRateLimiter(long limit, Duration windowSize) {
        this(limit, windowSize, MyWatch.SYSTEM_NANO_TIME);
    }

    public FixedWindowCounterRateLimiter(long limit, Duration windowSize, MyWatch watch) {
        if (limit <= 0) throw new IllegalArgumentException("limit must be > 0");
        Objects.requireNonNull(windowSize, "No windowSize");
        Objects.requireNonNull(watch, "No watch");
        this.watch = watch;
        this.limit = limit;
        this.windowSizeNanos = windowSize.toNanos();
        this.windowStartNanos = watch.currentTimeNanos();
        this.counterByResource = new ConcurrentHashMap<>();
    }

    // O(1)
    @Override
    public boolean isAllowed(String resource) {
        Objects.requireNonNull(resource, "No resource");
        long nowNanos = watch.currentTimeNanos();
        // Has the current window expired?
        if (nowNanos - windowStartNanos >= windowSizeNanos) {
            synchronized (this) {
                // Double-checked locking
                if (nowNanos - windowStartNanos >= windowSizeNanos) {
                    // Reset state
                    counterByResource.clear();
                    windowStartNanos = nowNanos;
                }
            }
        }
        // Get or create counter
        AtomicLong counter = counterByResource.computeIfAbsent(resource, k -> new AtomicLong(0));
        return counter.incrementAndGet() <= limit;
    }

}
