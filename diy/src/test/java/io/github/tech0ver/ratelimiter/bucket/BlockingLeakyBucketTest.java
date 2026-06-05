package io.github.tech0ver.ratelimiter.bucket;

import io.github.tech0ver.ratelimiter.MockedThreadParker;
import io.github.tech0ver.ratelimiter.MockedTimeSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockingLeakyBucketTest {

    static Stream<Arguments> provideLimiters() {
        return Stream.of(
                Arguments.argumentSet("ThreadUnsafe",
                        (Function<BlockingBucketConfig, BaseBlockingLeakyBucket>) ThreadUnsafeBlockingLeakyBucket::new),
                Arguments.argumentSet("Synchronized",
                        (Function<BlockingBucketConfig, BaseBlockingLeakyBucket>) SynchronizedBlockingLeakyBucket::new),
                Arguments.argumentSet("LockFree",
                        (Function<BlockingBucketConfig, BaseBlockingLeakyBucket>) LockFreeBlockingLeakyBucket::new)
        );
    }

    /**
     * <pre>
     *     t q  +  -
     * 0.000 0 +1 -1
     *  .250 0 +2 -1
     *  .500 1 +2 -1
     *  .750 2 +1 -1
     * 1.000 2    -1
     *  .250 1    -1
     *  .500 0 +5 -1 >1
     *  .750 3 +2 -1 >1
     * 2.000 3 +1 -1
     *  .250 3    -1
     *  .500 2    -1
     *  .750 1    -1
     * 3.000 0
     * </pre>
     */
    @ParameterizedTest
    @MethodSource("provideLimiters")
    void should_shape_traffic(Function<BlockingBucketConfig, BaseBlockingLeakyBucket> builder) {
        int capacity = 3;
        Duration interval = Duration.ofMillis(250);
        MockedTimeSource watch = new MockedTimeSource();
        MockedThreadParker parker = new MockedThreadParker();
        var limiter = builder.apply(new BlockingBucketConfig(capacity, interval, watch, parker));
        long nanosPerCount = interval.toNanos();
        // 0.000
        var reservation = limiter.reserveFreeTime();
        assertTrue(reservation.granted());
        assertEquals(0L, reservation.nanosToWait());
        watch.advance(nanosPerCount);
        // 0.250
        reservation = limiter.reserveFreeTime();
        assertTrue(reservation.granted());
        assertEquals(0L, reservation.nanosToWait());
        reservation = limiter.reserveFreeTime();
        assertTrue(reservation.granted());
        assertEquals(nanosPerCount, reservation.nanosToWait());
        reservation = limiter.reserveFreeTime();
        assertTrue(reservation.granted());
        assertEquals(nanosPerCount * 2, reservation.nanosToWait());
        reservation = limiter.reserveFreeTime();
        assertTrue(reservation.denied());
        assertEquals(1L, reservation.nanosToWait());
        watch.advance(nanosPerCount);
        // 0.500
        reservation = limiter.reserveFreeTime();
        assertTrue(reservation.granted());
        assertEquals(nanosPerCount * 2, reservation.nanosToWait());
        reservation = limiter.reserveFreeTime();
        assertTrue(reservation.denied());
        assertEquals(1L, reservation.nanosToWait());
        watch.advance(nanosPerCount);
        // 0.750
        reservation = limiter.reserveFreeTime();
        assertTrue(reservation.granted());
        assertEquals(nanosPerCount * 2, reservation.nanosToWait());
        watch.advance(nanosPerCount);
        // 1.000
        watch.advance(nanosPerCount);
        // 1.250
        reservation = limiter.reserveFreeTime();
        assertTrue(reservation.granted());
        assertEquals(nanosPerCount, reservation.nanosToWait());
        watch.advance(nanosPerCount);
        // 1.500
        reservation = limiter.reserveFreeTime();
        assertTrue(reservation.granted());
        assertEquals(nanosPerCount, reservation.nanosToWait());
        watch.advance(nanosPerCount);
        // 1.750
        watch.advance(nanosPerCount);
        // 2.000
        watch.advance(nanosPerCount);
    }

    @ParameterizedTest
    @MethodSource("provideLimiters")
    void should_reserve(Function<BlockingBucketConfig, BaseBlockingLeakyBucket> builder) {
        int capacity = 3;
        Duration interval = Duration.ofMillis(250);
        MockedTimeSource watch = new MockedTimeSource();
        MockedThreadParker parker = new MockedThreadParker();
        var limiter = builder.apply(new BlockingBucketConfig(capacity, interval, watch, parker));
        long nanosPerCount = interval.toNanos();
        var reservation = limiter.reserveFreeTime();
        assertTrue(reservation.granted());
        assertEquals(0L, reservation.nanosToWait());
        reservation = limiter.reserveFreeTime();
        assertTrue(reservation.granted());
        assertEquals(nanosPerCount, reservation.nanosToWait());
        reservation = limiter.reserveFreeTime();
        assertTrue(reservation.granted());
        assertEquals(nanosPerCount * 2, reservation.nanosToWait());
        reservation = limiter.reserveFreeTime();
        assertTrue(reservation.denied());
        assertEquals(1L, reservation.nanosToWait());
        reservation = limiter.reserveFreeTime();
        assertTrue(reservation.denied());
        assertEquals(1L, reservation.nanosToWait());
    }

    @ParameterizedTest
    @MethodSource("provideLimiters")
    void should_return_remaining(Function<BlockingBucketConfig, BaseBlockingLeakyBucket> builder) {
        int capacity = 3;
        Duration interval = Duration.ofMillis(250);
        MockedTimeSource watch = new MockedTimeSource();
        MockedThreadParker parker = new MockedThreadParker();
        var limiter = builder.apply(new BlockingBucketConfig(capacity, interval, watch, parker));
        var reservation = limiter.reserveFreeTime();
        assertTrue(reservation.granted());
        assertEquals(2L, reservation.remaining());
        reservation = limiter.reserveFreeTime();
        assertTrue(reservation.granted());
        assertEquals(1L, reservation.remaining());
        reservation = limiter.reserveFreeTime();
        assertTrue(reservation.granted());
        assertEquals(0L, reservation.remaining());
        reservation = limiter.reserveFreeTime();
        assertTrue(reservation.denied());
        assertEquals(0L, reservation.remaining());
    }

}
