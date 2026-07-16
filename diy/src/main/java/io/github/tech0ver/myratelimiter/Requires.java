package io.github.tech0ver.myratelimiter;

import java.time.Duration;

public final class Requires {

    private Requires() {
    }

    public static class InvalidValueException extends IllegalArgumentException {
        public InvalidValueException(String message) {
            super(message);
        }
    }

    public static <T> T nonNull(T value, String message) {
        if (value != null) return value;
        throw new InvalidValueException(message);
    }

    public static int positive(int value, String message) {
        if (value > 0) return value;
        throw new InvalidValueException(message);
    }

    public static long positive(long value, String message) {
        if (value > 0) return value;
        throw new InvalidValueException(message);
    }

    public static double positive(double value, String message) {
        if (value > 0.0) return value;
        throw new InvalidValueException(message);
    }

    public static Duration positive(Duration value, String message) {
        if (value != null && !value.isZero() && !value.isNegative()) return value;
        throw new InvalidValueException(message);
    }

    public static long positiveOrZero(long value, String message) {
        if (value >= 0) return value;
        throw new InvalidValueException(message);
    }

    public static Duration positiveOrZero(Duration value, String message) {
        if (value != null && !value.isNegative()) return value;
        throw new InvalidValueException(message);
    }

    public static long between(long value, long min, long max, String message) {
        if (value >= min && value <= max) return value;
        throw new InvalidValueException(message);
    }

}
