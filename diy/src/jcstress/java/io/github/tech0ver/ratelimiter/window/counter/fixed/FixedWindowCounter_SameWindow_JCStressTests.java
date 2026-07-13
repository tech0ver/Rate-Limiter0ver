package io.github.tech0ver.ratelimiter.window.counter.fixed;


import io.github.tech0ver.ratelimiter.MyRateLimiter;
import io.github.tech0ver.ratelimiter.Requires;
import io.github.tech0ver.ratelimiter.TimeSource;
import io.github.tech0ver.ratelimiter.window.SameWindow;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.II_Result;

import java.time.Duration;

public class FixedWindowCounter_SameWindow_JCStressTests {

    @JCStressTest
    @Outcome(id = "0, 1", expect = Expect.ACCEPTABLE)
    @Outcome(id = "1, 0", expect = Expect.ACCEPTABLE)
    @Outcome(expect = Expect.ACCEPTABLE_INTERESTING)
    @State
    public static class ThreadUnsafe extends SameWindow {

        @Override
        protected MyRateLimiter createLimiter(int limit, Duration windowSize, TimeSource ts) {
            return new ThreadUnsafeFixedWindowCounter(limit, windowSize, ts);
        }

        @Actor
        @Override
        public void actor1(II_Result r) {
            try {
                super.actor1(r);
            } catch (Requires.InvalidValueException ex) {
                r.r1 = -1;
            }
        }

        @Actor
        @Override
        public void actor2(II_Result r) {
            try {
                super.actor2(r);
            } catch (Requires.InvalidValueException ex) {
                r.r2 = -1;
            }
        }

    }

    @JCStressTest
    @JCStressMeta(SameWindow.class)
    @State
    public static class Synchronized extends SameWindow {

        @Override
        protected MyRateLimiter createLimiter(int limit, Duration windowSize, TimeSource ts) {
            return new SynchronizedFixedWindowCounter(limit, windowSize, ts);
        }

        @Actor
        @Override
        public void actor1(II_Result r) {
            super.actor1(r);
        }

        @Actor
        @Override
        public void actor2(II_Result r) {
            super.actor2(r);
        }

    }

    @JCStressTest
    @JCStressMeta(SameWindow.class)
    @State
    public static class LockFree extends SameWindow {

        @Override
        protected MyRateLimiter createLimiter(int limit, Duration windowSize, TimeSource ts) {
            return new LockFreeFixedWindowCounter(limit, windowSize, ts);
        }

        @Actor
        @Override
        public void actor1(II_Result r) {
            super.actor1(r);
        }

        @Actor
        @Override
        public void actor2(II_Result r) {
            super.actor2(r);
        }

    }

}
