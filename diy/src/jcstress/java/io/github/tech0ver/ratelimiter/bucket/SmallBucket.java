package io.github.tech0ver.ratelimiter.bucket;

import io.github.tech0ver.ratelimiter.MyRateLimiter;
import io.github.tech0ver.ratelimiter.TimeSource;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.infra.results.III_Result;

import java.time.Duration;

@Outcome(id = "0, 1, 1", expect = Expect.ACCEPTABLE)
@Outcome(id = "1, 0, 1", expect = Expect.ACCEPTABLE)
@Outcome(id = "1, 1, 0", expect = Expect.ACCEPTABLE)
@Outcome(id = "1, 1, 1", expect = Expect.FORBIDDEN)
@Outcome(expect = Expect.FORBIDDEN)
public abstract class SmallBucket {

    private final TimeSource ts = () -> 0L;
    private final MyRateLimiter limiter = createLimiter(2, Duration.ofMillis(100L), ts);

    protected abstract MyRateLimiter createLimiter(long capacity, Duration interval, TimeSource ts);

    public void actor1(III_Result r) {
        r.r1 = limiter.isGranted() ? 1 : 0;
    }

    public void actor2(III_Result r) {
        r.r2 = limiter.isGranted() ? 1 : 0;
    }

    public void actor3(III_Result r) {
        r.r3 = limiter.isGranted() ? 1 : 0;
    }

}
