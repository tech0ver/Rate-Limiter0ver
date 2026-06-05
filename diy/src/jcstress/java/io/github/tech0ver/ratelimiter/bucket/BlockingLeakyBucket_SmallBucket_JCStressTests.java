package io.github.tech0ver.ratelimiter.bucket;


import io.github.tech0ver.ratelimiter.MyRateLimiter;
import io.github.tech0ver.ratelimiter.ThreadParker;
import io.github.tech0ver.ratelimiter.TimeSource;
import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.JCStressMeta;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.III_Result;

import java.time.Duration;

public class BlockingLeakyBucket_SmallBucket_JCStressTests {

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
