package io.github.tech0ver.ratelimiter.window;

import io.github.tech0ver.ratelimiter.Requires;
import io.github.tech0ver.ratelimiter.TimeSource;
import io.github.tech0ver.ratelimiter.window.BaseWindowRateLimiter;
import io.github.tech0ver.ratelimiter.window.WindowConfig;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Queue;

public class ThreadUnsafeSlidingWindowLog extends BaseWindowRateLimiter {

    // State O(limit)
    private final Queue<Long> logs;

    public ThreadUnsafeSlidingWindowLog(WindowConfig config) {
        this((int) Requires.between(config.limit(), 1L, Integer.MAX_VALUE,
                        "limit must be between 1.." + Integer.MAX_VALUE),
                config.windowSize(), config.timeSource());
    }

    public ThreadUnsafeSlidingWindowLog(int limit, Duration windowSize) {
        this(limit, windowSize, TimeSource.MONOTONIC);
    }

    public ThreadUnsafeSlidingWindowLog(int limit, Duration windowSize, TimeSource timeSource) {
        super(limit, windowSize, timeSource);
        this.logs = new ArrayDeque<>(limit);
    }

    // O(limit)
    @Override
    public Decision tryPermit() {
        long nowNanos = timeSource.currentTimeNanos();
        long elapsedNanos = 0L;
        while (!logs.isEmpty() && (elapsedNanos = nowNanos - logs.peek()) >= windowSizeNanos) {
            logs.poll();
        }
        if (logs.size() >= limit) {
            long remainingNanos = windowSizeNanos - elapsedNanos;
            return Decision.asDenied(Math.max(remainingNanos, 0L));
        }
        logs.offer(nowNanos);
        return Decision.asGranted(limit - logs.size());
    }

}
