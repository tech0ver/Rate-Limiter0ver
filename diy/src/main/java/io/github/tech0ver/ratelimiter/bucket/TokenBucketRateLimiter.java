package io.github.tech0ver.ratelimiter.bucket;

import io.github.tech0ver.ratelimiter.MyRateLimiter;
import io.github.tech0ver.ratelimiter.MyWatch;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class TokenBucketRateLimiter implements MyRateLimiter {

    // O(N) where N is the number of active resources
    private final Map<String, Bucket> bucketByResource;
    private final MyWatch watch;
    private final long capacity;
    private final double tokensPerNano;

    public TokenBucketRateLimiter(long capacity, Duration refillPeriod) {
        this(capacity, refillPeriod, MyWatch.SYSTEM_NANO_TIME);
    }

    public TokenBucketRateLimiter(long capacity, Duration refillPeriod, MyWatch watch) {
        this(capacity, 1, refillPeriod, watch);
    }

    public TokenBucketRateLimiter(long capacity, long refillTokens, Duration refillPeriod, MyWatch watch) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        if (refillTokens <= 0) throw new IllegalArgumentException("refillTokens must be > 0");
        Objects.requireNonNull(refillPeriod, "No refillPeriod");
        Objects.requireNonNull(watch, "No watch");
        this.watch = watch;
        this.capacity = capacity;
        this.tokensPerNano = refillTokens / (double) refillPeriod.toNanos();
        this.bucketByResource = new ConcurrentHashMap<>();
    }

    // O(1)
    @Override
    public boolean isAllowed(String resource) {
        Objects.requireNonNull(resource, "No resource");
        long nowNanos = watch.currentTimeNanos();
        Bucket bucket = bucketByResource.computeIfAbsent(
                resource, k -> new Bucket(nowNanos)
        );
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (bucket) {
            if (nowNanos > bucket.lastRefillNanos) {
                long elapsedSinceLastRefillNanos = nowNanos - bucket.lastRefillNanos;
                double refilledTokens = elapsedSinceLastRefillNanos * tokensPerNano;
                bucket.tokens = Math.min(capacity, bucket.tokens + refilledTokens);
                bucket.lastRefillNanos = nowNanos;
            }
            if (bucket.tokens >= 1.0) {
                bucket.tokens -= 1.0;
                return true;
            }
            return false;
        }
    }

    private class Bucket {

        double tokens;
        long lastRefillNanos;

        public Bucket(long lastRefillNanos) {
            this.tokens = capacity;
            this.lastRefillNanos = lastRefillNanos;
        }

    }

}
