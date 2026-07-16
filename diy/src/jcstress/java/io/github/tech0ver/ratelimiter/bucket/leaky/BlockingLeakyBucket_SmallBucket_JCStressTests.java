package io.github.tech0ver.ratelimiter.bucket.leaky;


import io.github.tech0ver.ratelimiter.MyRateLimiter;
import io.github.tech0ver.ratelimiter.Requires;
import io.github.tech0ver.ratelimiter.ThreadParker;
import io.github.tech0ver.ratelimiter.TimeSource;
import io.github.tech0ver.ratelimiter.bucket.SmallBlockingBucket;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.III_Result;

import java.time.Duration;

public class BlockingLeakyBucket_SmallBucket_JCStressTests {

    @JCStressTest
    @Outcome(id = "0, 1, 1", expect = Expect.ACCEPTABLE)
    @Outcome(id = "1, 0, 1", expect = Expect.ACCEPTABLE)
    @Outcome(id = "1, 1, 0", expect = Expect.ACCEPTABLE)
    @Outcome(expect = Expect.ACCEPTABLE_INTERESTING)
    @State
    public static class ThreadUnsafe extends SmallBlockingBucket {

        @Override
        protected MyRateLimiter createLimiter(long capacity, Duration interval, TimeSource ts, ThreadParker parker) {
            return new ThreadUnsafeBlockingLeakyBucket(capacity, interval, ts, parker);
        }

        @Actor
        @Override
        public void actor1(III_Result r) {
            try {
                super.actor1(r);
            } catch (Requires.InvalidValueException ex) {
                r.r1 = -1;
            }
        }

        @Actor
        @Override
        public void actor2(III_Result r) {
            try {
                super.actor2(r);
            } catch (Requires.InvalidValueException ex) {
                r.r2 = -1;
            }
        }

        @Actor
        @Override
        public void actor3(III_Result r) {
            try {
                super.actor3(r);
            } catch (Requires.InvalidValueException ex) {
                r.r3 = -1;

            }
        }

    }

    @JCStressTest
    @JCStressMeta(SmallBlockingBucket.class)
    @State
    public static class Synchronized extends SmallBlockingBucket {

        @Override
        protected MyRateLimiter createLimiter(long capacity, Duration interval, TimeSource ts, ThreadParker parker) {
            return new SynchronizedBlockingLeakyBucket(capacity, interval, ts, parker);
        }

        @Actor
        @Override
        public void actor1(III_Result r) {
            super.actor1(r);
        }

        @Actor
        @Override
        public void actor2(III_Result r) {
            super.actor2(r);
        }

        @Actor
        @Override
        public void actor3(III_Result r) {
            super.actor3(r);
        }

    }

    @JCStressTest
    @JCStressMeta(SmallBlockingBucket.class)
    @State
    public static class LockFree extends SmallBlockingBucket {

        @Override
        protected MyRateLimiter createLimiter(long capacity, Duration interval, TimeSource ts, ThreadParker parker) {
            return new LockFreeBlockingLeakyBucket(capacity, interval, ts, parker);
        }

        @Actor
        @Override
        public void actor1(III_Result r) {
            super.actor1(r);
        }

        @Actor
        @Override
        public void actor2(III_Result r) {
            super.actor2(r);
        }

        @Actor
        @Override
        public void actor3(III_Result r) {
            super.actor3(r);
        }

    }

}
