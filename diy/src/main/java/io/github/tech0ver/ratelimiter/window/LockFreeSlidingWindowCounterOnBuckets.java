package io.github.tech0ver.ratelimiter.window;

import io.github.tech0ver.ratelimiter.Requires;
import io.github.tech0ver.ratelimiter.TimeSource;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

public class LockFreeSlidingWindowCounterOnBuckets extends BaseWindowRateLimiter {

    // Configuration O(1)
    private final int numberOfBuckets;
    private final long bucketSizeNanos;

    // State O(numberOfBuckets)
    private record Bucket(AtomicLong startNanos, AtomicLong counter) {}
    private final Bucket[] buckets;
    private final AtomicLong totalCount;

    public LockFreeSlidingWindowCounterOnBuckets(BucketedWindowConfig config) {
        this(config.limit(), config.windowSize(), config.numberOfBuckets(), config.timeSource());
    }

    public LockFreeSlidingWindowCounterOnBuckets(long limit, Duration windowSize, int numberOfBuckets) {
        this(limit, windowSize, numberOfBuckets, TimeSource.MONOTONIC);
    }

    public LockFreeSlidingWindowCounterOnBuckets(long limit, Duration windowSize, int numberOfBuckets, TimeSource timeSource) {
        super(limit, windowSize, timeSource);
        this.numberOfBuckets = Requires.positive(numberOfBuckets, "numberOfBuckets must be > 0");
        this.bucketSizeNanos = windowSize.toNanos() / numberOfBuckets;
        this.buckets = new Bucket[numberOfBuckets];
        long nowNanos = timeSource.currentTimeNanos();
        long bucketStartNanos = nowNanos - (nowNanos % bucketSizeNanos);
        for (int i = 0; i < buckets.length; i++) {
            buckets[i] = new Bucket(new AtomicLong(bucketStartNanos), new AtomicLong());
        }
        this.totalCount = new AtomicLong();
    }

    // O(numberOfBuckets)
    @Override
    public Decision tryPermit() {
        long nowNanos = timeSource.currentTimeNanos();
        int currentBucketIndex = (int) ((nowNanos / bucketSizeNanos) % numberOfBuckets);
        long currentBucketStartNanos = nowNanos - (nowNanos % bucketSizeNanos);
        for (int i = 0; i < numberOfBuckets; i++) { // clean up
            long observedStartNanos = buckets[i].startNanos.get();
            int distance = (currentBucketIndex - i + numberOfBuckets) % numberOfBuckets;
            long expectedStartNanos = currentBucketStartNanos - distance * bucketSizeNanos;
            if (observedStartNanos != expectedStartNanos) {
                if (buckets[i].startNanos.compareAndSet(observedStartNanos, expectedStartNanos)) {
                    long oldCount = buckets[i].counter.getAndSet(0L);
                    if (oldCount != 0L) {
                        totalCount.addAndGet(-oldCount);
                    }
                }
            }
        }
        long newTotalCount = totalCount.incrementAndGet();
        if (newTotalCount <= limit) {
            buckets[currentBucketIndex].counter.incrementAndGet(); // commit
            return Decision.asGranted(limit - newTotalCount);
        } else {
            totalCount.decrementAndGet(); // rollback
            long oldestBucketStartNanos = buckets[currentBucketIndex].startNanos.get();
            for (int i = 0; i < numberOfBuckets; i++) {
                long bucketStartNanos = buckets[i].startNanos.get();
                if (oldestBucketStartNanos > bucketStartNanos && buckets[i].counter.get() > 0L) {
                    oldestBucketStartNanos = bucketStartNanos;
                }
            }
            long elapsedNanos = nowNanos - oldestBucketStartNanos;
            long remainingNanos = windowSizeNanos - elapsedNanos;
            return Decision.asDenied(Math.max(remainingNanos, 0L));
        }
    }

}
