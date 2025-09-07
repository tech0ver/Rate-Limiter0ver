package io.github.tech0ver.ratelimiter.bucket;

import io.github.tech0ver.ratelimiter.MyRateLimiter;
import io.github.tech0ver.ratelimiter.MyWatch;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class LeakyBucketRateLimiter implements MyRateLimiter {

    // O(N) where N is the number of active resources
    private final Map<String, Bucket> bucketByResource;
    private final MyWatch watch;
    private final long capacity;
    private final double leakPerNano;

    public LeakyBucketRateLimiter(long capacity, Duration leakPeriod) {
        this(capacity, 1, leakPeriod, MyWatch.SYSTEM_NANO_TIME);
    }

    public LeakyBucketRateLimiter(long capacity, long leakCount, Duration leakPeriod, MyWatch watch) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        if (leakCount <= 0) throw new IllegalArgumentException("leakCount must be > 0");
        Objects.requireNonNull(leakPeriod, "No leakPeriod");
        Objects.requireNonNull(watch, "No watch");
        this.watch = watch;
        this.capacity = capacity;
        this.leakPerNano = leakCount / (double) leakPeriod.toNanos();
        this.bucketByResource = new ConcurrentHashMap<>();
    }

    // O(1)
    @Override
    public Decision decide(String resource) {
        Objects.requireNonNull(resource, "No resource");
        long nowNanos = watch.currentTimeNanos();
        Bucket bucket = bucketByResource.computeIfAbsent(resource, k -> new Bucket(nowNanos));
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (bucket) {
            if (nowNanos > bucket.lastUpdateNanos) {
                long elapsedSinceLastUpdateNanos = nowNanos - bucket.lastUpdateNanos;
                double leakedCount = elapsedSinceLastUpdateNanos * leakPerNano;
                bucket.level = Math.max(0.0, bucket.level - leakedCount);
                bucket.lastUpdateNanos = nowNanos;
            }
            if (bucket.level + 1.0 <= capacity) {
                bucket.level += 1.0;
                return Decision.fromNanos(true, 0L);
            }
            double overflow = bucket.level + 1.0 - capacity;
            long remainingNanos = (long) Math.ceil(overflow / leakPerNano);
            return Decision.fromNanos(false, Math.max(remainingNanos, 0L));
        }
    }

    private static final class Bucket {

        double level;
        long lastUpdateNanos;

        Bucket(long lastUpdateNanos) {
            this.lastUpdateNanos = lastUpdateNanos;
        }

    }

}
