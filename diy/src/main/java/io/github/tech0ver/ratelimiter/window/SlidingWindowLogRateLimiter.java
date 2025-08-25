package io.github.tech0ver.ratelimiter.window;

import io.github.tech0ver.ratelimiter.MyRateLimiter;
import io.github.tech0ver.ratelimiter.MyWatch;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class SlidingWindowLogRateLimiter implements MyRateLimiter {

    // O(N * L) where N is the number of active resources, L is the limit
    private final Map<String, Deque<Long>> logsByResource;
    private final MyWatch watch;
    private final int limit;
    private final long windowSizeNanos;

    public SlidingWindowLogRateLimiter(int limit, Duration windowSize) {
        this(limit, windowSize, MyWatch.SYSTEM_NANO_TIME);
    }

    public SlidingWindowLogRateLimiter(int limit, Duration windowSize, MyWatch watch) {
        if (limit <= 0) throw new IllegalArgumentException("limit must be > 0");
        Objects.requireNonNull(windowSize, "No windowSize");
        Objects.requireNonNull(watch, "No watch");
        this.watch = watch;
        this.limit = limit;
        this.windowSizeNanos = windowSize.toNanos();
        this.logsByResource = new ConcurrentHashMap<>();
    }

    // O(L) where L is the limit
    @Override
    public boolean isAllowed(String resource) {
        Objects.requireNonNull(resource, "No resource");
        long nowNanos = watch.currentTimeNanos();
        // Get or create logs
        Deque<Long> logs = logsByResource.computeIfAbsent(resource, k -> new ArrayDeque<>());
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (logs) {
            // Has the log expired?
            while (!logs.isEmpty() && nowNanos - logs.getFirst() >= windowSizeNanos) {
                logs.removeFirst();
            }
            // Logs not filled?
            if (logs.size() < limit) {
                logs.addLast(nowNanos);
                return true;
            }
            return false;
        }
    }

}
