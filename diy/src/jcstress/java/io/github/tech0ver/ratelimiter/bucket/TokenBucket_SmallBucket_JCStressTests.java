package io.github.tech0ver.ratelimiter.bucket;


import io.github.tech0ver.ratelimiter.MyRateLimiter;
import io.github.tech0ver.ratelimiter.TimeSource;
import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressMeta;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.III_Result;

import java.time.Duration;

public class TokenBucket_SmallBucket_JCStressTests {

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
