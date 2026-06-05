package io.github.tech0ver.ratelimiter;

import java.time.Clock;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

public interface TimeSource {

    long currentTimeNanos();

    default boolean isWallClock() { return true; }

    /**
     * Based on {@link System#nanoTime}
     */
    TimeSource MONOTONIC = new TimeSource() {

        @Override public long currentTimeNanos() { return System.nanoTime(); }

        @Override public boolean isWallClock() { return false; }

        @Override public String toString() { return "MonotonicTimeSource"; }

    };

    /**
     * Based on {@link System#currentTimeMillis}
     */
    TimeSource WALL = new TimeSource() {

        @Override public long currentTimeNanos() { return TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()); }

        @Override public String toString() { return "WallTimeSource"; }

    };

    static TimeSource of(Clock clock) {
        Requires.nonNull(clock, "No clock");
        return new TimeSource() {

            @Override public long currentTimeNanos() { return TimeUnit.MILLISECONDS.toNanos(clock.millis()); }

            @Override public String toString() { return "ClockTimeSource"; }

        };
    }

    static TimeSource from(LongSupplier timeSupplier) {
        Requires.nonNull(timeSupplier, "No timeSupplier");
        return new TimeSource() {

            @Override public long currentTimeNanos() { return timeSupplier.getAsLong(); }

            @Override public String toString() { return "SuppliedTimeSource"; }

        };
    }

}
