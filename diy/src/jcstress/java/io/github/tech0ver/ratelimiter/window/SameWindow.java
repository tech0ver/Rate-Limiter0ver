package io.github.tech0ver.ratelimiter.window;

import io.github.tech0ver.ratelimiter.MyRateLimiter;
import io.github.tech0ver.ratelimiter.TimeSource;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.infra.results.II_Result;

import java.time.Duration;


@Outcome(id = "0, 1", expect = Expect.ACCEPTABLE)
@Outcome(id = "1, 0", expect = Expect.ACCEPTABLE)
@Outcome(id = "1, 1", expect = Expect.FORBIDDEN)
@Outcome(expect = Expect.FORBIDDEN)
public abstract class SameWindow {

    private final TimeSource ts = () -> 0L;
    private final MyRateLimiter limiter = createLimiter(1, Duration.ofMillis(100L), ts);

    protected abstract MyRateLimiter createLimiter(int limit, Duration windowSize, TimeSource ts);

    public void actor1(II_Result r) {
        r.r1 = limiter.isGranted() ? 1 : 0;
    }

    public void actor2(II_Result r) {
        r.r2 = limiter.isGranted() ? 1 : 0;
    }

}
