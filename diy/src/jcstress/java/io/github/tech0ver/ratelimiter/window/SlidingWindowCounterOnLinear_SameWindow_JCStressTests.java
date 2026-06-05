package io.github.tech0ver.ratelimiter.window;


import io.github.tech0ver.ratelimiter.MyRateLimiter;
import io.github.tech0ver.ratelimiter.TimeSource;
import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressMeta;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.II_Result;

import java.time.Duration;

public class SlidingWindowCounterOnLinear_SameWindow_JCStressTests {

    @JCStressTest
    @JCStressMeta(SameWindow.class)
    @State
    public static class Synchronized extends SameWindow {

        @Override
        protected MyRateLimiter createLimiter(int limit, Duration windowSize, TimeSource ts) {
            return new SynchronizedSlidingWindowCounterOnLinear(limit, windowSize, ts);
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
            return new LockFreeSlidingWindowCounterOnLinear(limit, windowSize, ts);
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
