package io.github.tech0ver.demo.ratelimiter.factory;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.local.LocalBucket;
import io.github.tech0ver.demo.ratelimiter.RateLimitRule;
import io.github.tech0ver.demo.ratelimiter.RateLimiter;
import io.github.tech0ver.demo.ratelimiter.adapter.Bucket4JRateLimiterAdapter;
import io.github.tech0ver.demo.ratelimiter.adapter.DiyRateLimiterAdapter;
import io.github.tech0ver.ratelimiter.ThreadParker;
import io.github.tech0ver.ratelimiter.TimeSource;
import io.github.tech0ver.ratelimiter.bucket.LockFreeBlockingLeakyBucket;
import io.github.tech0ver.ratelimiter.bucket.LockFreeTokenBucket;
import io.github.tech0ver.ratelimiter.window.LockFreeFixedWindowCounter;
import io.github.tech0ver.ratelimiter.window.LockFreeSlidingWindowCounterOnBuckets;
import io.github.tech0ver.ratelimiter.window.LockFreeSlidingWindowCounterOnLinear;
import io.github.tech0ver.ratelimiter.window.LockFreeSlidingWindowLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SwitchRateLimiterFactory implements RateLimiterFactory {

    private final TimeSource timeSource;
    private final ThreadParker threadParker;

    @Override
    public RateLimiter createLimiterByRule(RateLimitRule rule) {
        return switch (rule.limiterType()) {
            case FixedWindow -> {
                var params = rule.fixedWindow();
                yield new DiyRateLimiterAdapter(new LockFreeFixedWindowCounter(
                        params.limit(), params.window(), timeSource
                ));
            }
            case SlidingLog -> {
                var params = rule.slidingLog();
                yield new DiyRateLimiterAdapter(new LockFreeSlidingWindowLog(
                        params.limit(), params.window(), timeSource
                ));
            }
            case SlidingBuckets -> {
                var params = rule.slidingBuckets();
                yield new DiyRateLimiterAdapter(new LockFreeSlidingWindowCounterOnBuckets(
                        params.limit(), params.window(), params.buckets(), timeSource
                ));
            }
            case SlidingLinear -> {
                var params = rule.slidingLinear();
                yield new DiyRateLimiterAdapter(new LockFreeSlidingWindowCounterOnLinear(
                        params.limit(), params.window(), timeSource
                ));
            }
            case TokenBucket -> {
                var params = rule.tokenBucket();
                yield new DiyRateLimiterAdapter(new LockFreeTokenBucket(
                        params.capacity(), params.tokens(), params.refill(), timeSource
                ));
            }
            case LeakyBucket -> {
                var params = rule.leakyBucket();
                yield new DiyRateLimiterAdapter(new LockFreeBlockingLeakyBucket(
                        params.capacity(), params.interval(), timeSource, threadParker
                ));
            }
            case Bucket4j -> {
                var params = rule.bucket4J();
                var builder = Bandwidth.builder().capacity(params.capacity());
                Bandwidth bandwidth;
                if (params.intervally()) {
                    bandwidth = builder.refillIntervally(params.tokens(), params.refill()).build();
                } else {
                    bandwidth = builder.refillGreedy(params.tokens(), params.refill()).build();
                }
                LocalBucket bucket = Bucket.builder().addLimit(bandwidth).build();
                yield new Bucket4JRateLimiterAdapter(bucket);
            }
        };
    }

}
