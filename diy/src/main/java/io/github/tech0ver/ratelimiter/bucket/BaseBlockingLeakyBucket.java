package io.github.tech0ver.ratelimiter.bucket;

import io.github.tech0ver.ratelimiter.MyBlockingRateLimiter;
import io.github.tech0ver.ratelimiter.Requires;
import io.github.tech0ver.ratelimiter.ThreadParker;
import io.github.tech0ver.ratelimiter.TimeSource;

import java.time.Duration;

public abstract class BaseBlockingLeakyBucket implements MyBlockingRateLimiter {

    // Configuration O(1)
    protected final long capacityNanos;
    protected final long nanosPerCount;
    protected final TimeSource timeSource;
    protected final ThreadParker threadParker;

    public BaseBlockingLeakyBucket(BlockingBucketConfig config) {
        this(config.capacity(), config.interval(), config.timeSource(), config.threadParker());
    }

    public BaseBlockingLeakyBucket(long capacity, Duration interval) {
        this(capacity, interval, TimeSource.MONOTONIC, ThreadParker.DEFAULT);
    }

    public BaseBlockingLeakyBucket(long capacity, Duration interval, TimeSource timeSource, ThreadParker threadParker) {
        this.nanosPerCount = Requires.positive(interval, "interval must be > 0").toNanos();
        this.capacityNanos = Requires.positive(capacity, "capacity must be > 0") * nanosPerCount;
        this.timeSource = Requires.nonNull(timeSource, "No timeSource");
        this.threadParker = Requires.nonNull(threadParker, "No threadParker");
    }

    @Override
    public long permit() {
        long totalWaitNanos = 0L;
        while (true) {
            Reservation reservation = reserveFreeTime();
            long waitNanos = reservation.nanosToWait();
            threadParker.park(waitNanos);
            totalWaitNanos += waitNanos;
            if (reservation.granted()) return totalWaitNanos;
        }
    }

    @Override
    public long permitInterruptibly() throws InterruptedException {
        long totalWaitNanos = 0L;
        while (true) {
            Reservation reservation = reserveFreeTime();
            long waitNanos = reservation.nanosToWait();
            threadParker.parkInterruptibly(waitNanos);
            totalWaitNanos += waitNanos;
            if (reservation.granted()) return totalWaitNanos;
        }
    }

    @Override
    public BlockingDecision tryPermit() {
        Reservation reservation = reserveFreeTime();
        long waitNanos = reservation.nanosToWait();
        if (reservation.denied()) return BlockingDecision.asDenied(waitNanos);
        threadParker.park(waitNanos);
        return BlockingDecision.asGranted(reservation.remaining(), waitNanos);
    }

    @Override
    public BlockingDecision tryPermitInterruptibly() throws InterruptedException {
        Reservation reservation = reserveFreeTime();
        long waitNanos = reservation.nanosToWait();
        if (reservation.denied()) return BlockingDecision.asDenied(waitNanos);
        threadParker.parkInterruptibly(waitNanos);
        return BlockingDecision.asGranted(reservation.remaining(), waitNanos);
    }

    @Override
    public BlockingDecision tryPermit(Duration waitTime) {
        long maxWaitNanos = Requires.positiveOrZero(waitTime, "waitTime must be >= 0").toNanos();
        Reservation reservation = reserveFreeTime(maxWaitNanos);
        long waitNanos = reservation.nanosToWait();
        if (reservation.denied()) return BlockingDecision.asDenied(waitNanos);
        threadParker.park(waitNanos);
        return BlockingDecision.asGranted(reservation.remaining(), waitNanos);
    }

    @Override
    public BlockingDecision tryPermitInterruptibly(Duration waitTime) throws InterruptedException {
        long maxWaitNanos = Requires.positiveOrZero(waitTime, "waitTime must be >= 0").toNanos();
        Reservation reservation = reserveFreeTime(maxWaitNanos);
        long waitNanos = reservation.nanosToWait();
        if (reservation.denied()) return BlockingDecision.asDenied(waitNanos);
        threadParker.parkInterruptibly(waitNanos);
        return BlockingDecision.asGranted(reservation.remaining(), waitNanos);
    }

    protected Reservation reserveFreeTime() {
        return reserveFreeTime(-1L);
    }

    protected abstract Reservation reserveFreeTime(long maxWaitNanos);

    protected interface Reservation {

        long nanosToWait();

        boolean granted();

        default long remaining() { return 0L; }

        default boolean denied() { return !granted(); }

        record Granted(long remaining, long nanosToWait) implements Reservation {

            @Override public boolean granted() { return true; }

        }

        record Denied(long nanosToWait) implements Reservation {

            @Override public boolean granted() { return false; }

        }

    }

}
