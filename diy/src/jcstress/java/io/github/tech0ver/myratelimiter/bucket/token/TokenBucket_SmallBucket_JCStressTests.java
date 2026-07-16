package io.github.tech0ver.myratelimiter.bucket.token;


import io.github.tech0ver.myratelimiter.MyRateLimiter;
import io.github.tech0ver.myratelimiter.Requires;
import io.github.tech0ver.myratelimiter.TimeSource;
import io.github.tech0ver.myratelimiter.bucket.SmallBucket;
import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.III_Result;

import java.time.Duration;

public class TokenBucket_SmallBucket_JCStressTests {

    @JCStressTest
    @Outcome(id = "0, 1, 1", expect = Expect.ACCEPTABLE)
    @Outcome(id = "1, 0, 1", expect = Expect.ACCEPTABLE)
    @Outcome(id = "1, 1, 0", expect = Expect.ACCEPTABLE)
    @Outcome(expect = Expect.ACCEPTABLE_INTERESTING)
    @State
    public static class ThreadUnsafe extends SmallBucket {

        @Override
        protected MyRateLimiter createLimiter(long capacity, Duration interval, TimeSource ts) {
            return new ThreadUnsafeTokenBucket(capacity, interval, ts);
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
    @JCStressMeta(SmallBucket.class)
    @State
    public static class Synchronized extends SmallBucket {

        @Override
        protected MyRateLimiter createLimiter(long capacity, Duration interval, TimeSource ts) {
            return new SynchronizedTokenBucket(capacity, interval, ts);
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
    @JCStressMeta(SmallBucket.class)
    @State
    public static class LockFree extends SmallBucket {

        @Override
        protected MyRateLimiter createLimiter(long capacity, Duration interval, TimeSource ts) {
            return new LockFreeTokenBucket(capacity, interval, ts);
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
