package io.github.tech0ver.ratelimiter.window;

import io.github.tech0ver.ratelimiter.TimeSource;

import java.time.Duration;
import java.util.Objects;

public class WindowConfig {

    protected final long limit;
    protected final Duration windowSize;
    protected final TimeSource timeSource;

    public WindowConfig(
            long limit,
            Duration windowSize,
            TimeSource timeSource
    ) {
        this.limit = limit;
        this.windowSize = windowSize;
        this.timeSource = timeSource;
    }

    public long limit() {
        return limit;
    }

    public Duration windowSize() {
        return windowSize;
    }

    public TimeSource timeSource() {
        return timeSource;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (WindowConfig) obj;
        return this.limit == that.limit &&
               Objects.equals(this.windowSize, that.windowSize) &&
               Objects.equals(this.timeSource, that.timeSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(limit, windowSize, timeSource);
    }

    @Override
    public String toString() {
        return "WindowConfig[" +
               "limit=" + limit + ", " +
               "windowSize=" + windowSize + ", " +
               "timeSource=" + timeSource + ']';
    }

}
