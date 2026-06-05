package io.github.tech0ver.ratelimiter.window;

import io.github.tech0ver.ratelimiter.TimeSource;

import java.time.Duration;
import java.util.Objects;

public class BucketedWindowConfig extends WindowConfig {

    private final int numberOfBuckets;

    public BucketedWindowConfig(long limit, Duration windowSize, int numberOfBuckets, TimeSource timeSource) {
        super(limit, windowSize, timeSource);
        this.numberOfBuckets = numberOfBuckets;
    }

    public int numberOfBuckets() {
        return numberOfBuckets;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false;
        if (obj.getClass() != this.getClass()) return false;
        var that = (BucketedWindowConfig) obj;
        return this.numberOfBuckets == that.numberOfBuckets;
    }

    @Override
    public int hashCode() {
        return Objects.hash(limit, windowSize, timeSource, numberOfBuckets);
    }

    @Override
    public String toString() {
        return "BucketedWindowConfig[" +
               "limit=" + limit + ", " +
               "windowSize=" + windowSize + ", " +
               "numberOfBuckets=" + numberOfBuckets + ", " +
               "timeSource=" + timeSource + ']';
    }

}
