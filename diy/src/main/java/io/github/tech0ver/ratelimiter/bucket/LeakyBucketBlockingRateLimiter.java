package io.github.tech0ver.ratelimiter.bucket;

import io.github.tech0ver.ratelimiter.MyBlockingRateLimiter;
import io.github.tech0ver.ratelimiter.MyWatch;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class LeakyBucketBlockingRateLimiter implements MyBlockingRateLimiter {

    // O(N) where N is the number of active resources
    private final Map<String, Bucket> bucketByResource;
    private final MyWatch watch;
    private final long nanosPerRequest;
    private final long capacityNanos;

    public LeakyBucketBlockingRateLimiter(long capacity, Duration requestInterval) {
        this(capacity, requestInterval, MyWatch.SYSTEM_NANO_TIME);
    }

    public LeakyBucketBlockingRateLimiter(long capacity, Duration requestInterval, MyWatch watch) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        Objects.requireNonNull(requestInterval, "No requestInterval");
        Objects.requireNonNull(watch, "No watch");
        this.watch = watch;
        this.nanosPerRequest = requestInterval.toNanos();
        this.capacityNanos = capacity * nanosPerRequest;
        this.bucketByResource = new ConcurrentHashMap<>();
    }

    @Override
    public void acquire(String resource) throws InterruptedException {
        Objects.requireNonNull(resource, "No resource");
        doAcquiring(resource, -1);
    }

    @Override
    public boolean tryAcquire(String resource, long timeout, TimeUnit unit) throws InterruptedException {
        Objects.requireNonNull(resource, "No resource");
        if (timeout < 0) throw new IllegalArgumentException("capacity must be >= 0");
        Objects.requireNonNull(unit, "No unit");
        return doAcquiring(resource, watch.currentTimeNanos() + unit.toNanos(timeout));
    }

    private boolean doAcquiring(String resource, long deadlineNanos) throws InterruptedException {
        while (true) {
            long delayNanos = getDelayNanos(resource);
            if (delayNanos == 0L) return true;
            long waitNanos = delayNanos > 0 ? delayNanos : -delayNanos;
            if (deadlineNanos >= 0) {
                long remainingNanos = deadlineNanos - watch.currentTimeNanos();
                if (remainingNanos <= 0L || remainingNanos < waitNanos) return false;
            }
            LockSupport.parkNanos(waitNanos);
            if (Thread.interrupted()) throw new InterruptedException();
        }
    }

    /**
     * O(1)
     *
     * @return {@code 0} - now; {@code >0} - wait (reserved); {@code <0} - overflow-wait (no reservation)
     */
    long getDelayNanos(String resource) {
        Bucket bucket = bucketByResource.computeIfAbsent(resource, k -> new Bucket());
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (bucket) {
            long nowNanos = watch.currentTimeNanos();
            long earliestAllowedNanos = bucket.nextReadyNanos - capacityNanos;
            // Overflow?
            if (nowNanos < earliestAllowedNanos) {
                return nowNanos - earliestAllowedNanos; // < 0
            }
            long readyNanos = Math.max(nowNanos, bucket.nextReadyNanos);
            bucket.nextReadyNanos = readyNanos + nanosPerRequest;
            return Math.max(0L, readyNanos - nowNanos); // >= 0
        }
    }

    private static final class Bucket {

        long nextReadyNanos;

    }

}
