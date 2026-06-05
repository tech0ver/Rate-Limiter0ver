package io.github.tech0ver.ratelimiter.window;

import io.github.tech0ver.ratelimiter.Requires;
import io.github.tech0ver.ratelimiter.TimeSource;
import io.github.tech0ver.ratelimiter.window.BaseWindowRateLimiter;
import io.github.tech0ver.ratelimiter.window.WindowConfig;

import java.time.Duration;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class LockFreeSlidingWindowLog extends BaseWindowRateLimiter {

    // State O(limit)
    private final Deque<Long> logs;
    private final AtomicInteger counter;

    public LockFreeSlidingWindowLog(WindowConfig config) {
        this((int) Requires.between(config.limit(), 1L, Integer.MAX_VALUE,
                        "limit must be between 1.." + Integer.MAX_VALUE),
                config.windowSize(), config.timeSource());
    }

    public LockFreeSlidingWindowLog(int limit, Duration windowSize) {
        this(limit, windowSize, TimeSource.MONOTONIC);
    }

    public LockFreeSlidingWindowLog(int limit, Duration windowSize, TimeSource timeSource) {
        super(limit, windowSize, timeSource);
        this.logs = new ConcurrentLinkedDeque<>();
        this.counter = new AtomicInteger();
    }

    // O(limit)
    @Override
    public Decision tryPermit() {
        while (true) {
            long nowNanos = timeSource.currentTimeNanos();
            long thresholdNanos = nowNanos - windowSizeNanos;
            while (true) { // clean up
                Long logNanos = logs.peekFirst();
                if (logNanos == null || logNanos > thresholdNanos)
                    break;
                Long removedLogNanos = logs.pollFirst();
                if (removedLogNanos == null)
                    continue;
                if (removedLogNanos <= thresholdNanos) {
                    counter.decrementAndGet();
                } else {
                    logs.addFirst(removedLogNanos);
                    break;
                }
            }
            int count = counter.get();
            if (count >= limit) {
                long remainingNanos = 0L;
                Long logNanos = logs.peek();
                if (logNanos != null) {
                    long elapsedNanos = nowNanos - logNanos;
                    remainingNanos = windowSizeNanos - elapsedNanos;
                }
                return Decision.asDenied(Math.max(remainingNanos, 0L));
            }
            int newCount = count + 1;
            if (counter.compareAndSet(count, newCount)) {
                logs.offer(nowNanos);
                return Decision.asGranted(limit - newCount);
            }
        }
    }

}
