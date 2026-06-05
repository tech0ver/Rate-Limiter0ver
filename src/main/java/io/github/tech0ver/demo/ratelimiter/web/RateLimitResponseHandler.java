package io.github.tech0ver.demo.ratelimiter.web;

import io.github.tech0ver.demo.config.props.RateLimitProps;
import io.github.tech0ver.demo.ratelimiter.ResourceRateLimiter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@SuppressWarnings("UastIncorrectHttpHeaderInspection")
public class RateLimitResponseHandler {

    private final Clock clock;
    private final RateLimitProps props;

    private void setBaseRateLimitHeaders(
            HttpServletResponse response, ResourceRateLimiter.Decision<String> decision
    ) {
        String remaining = String.valueOf(decision.remaining());
        response.setHeader("RateLimit-Remaining", remaining);
        response.setHeader("X-RateLimit-Remaining", remaining);
        var rule = decision.rule();
        switch (rule.limiterType()) {
            case FixedWindow -> {
                var params = rule.fixedWindow();
                response.setHeader("RateLimit-Limit", params.limit() + ";w=" + params.window().toSeconds());
                response.setHeader("X-RateLimit-Limit", String.valueOf(params.limit()));
                response.setHeader("X-RateLimit-Policy",
                        "fixed-window; limit=" + params.limit() + "; window=" + params.window());
            }
            case SlidingLog -> {
                var params = rule.slidingLog();
                response.setHeader("RateLimit-Limit", params.limit() + ";w=" + params.window().toSeconds());
                response.setHeader("X-RateLimit-Limit", String.valueOf(params.limit()));
                response.setHeader("X-RateLimit-Policy",
                        "sliding-log; limit=" + params.limit() + "; window=" + params.window());
            }
            case SlidingBuckets -> {
                var params = rule.slidingBuckets();
                response.setHeader("RateLimit-Limit", params.limit() + ";w=" + params.window().toSeconds());
                response.setHeader("X-RateLimit-Limit", String.valueOf(params.limit()));
                response.setHeader("X-RateLimit-Policy",
                        "sliding-buckets; limit=" + params.limit() + "; window=" + params.window() +
                        "; buckets=" + params.buckets());
            }
            case SlidingLinear -> {
                var params = rule.slidingLinear();
                response.setHeader("RateLimit-Limit", params.limit() + ";w=" + params.window().toSeconds());
                response.setHeader("X-RateLimit-Limit", String.valueOf(params.limit()));
                response.setHeader("X-RateLimit-Policy",
                        "sliding-linear; limit=" + params.limit() + "; window=" + params.window());
            }
            case TokenBucket -> {
                var params = rule.tokenBucket();
                response.setHeader("RateLimit-Limit", params.capacity() + ";w=" + params.refill().toSeconds());
                response.setHeader("X-RateLimit-Limit", String.valueOf(params.capacity()));
                response.setHeader("X-RateLimit-Policy",
                        "token-bucket; capacity=" + params.capacity() + "; refill=" + params.refill() +
                        "; tokens=" + params.tokens());
            }
            case LeakyBucket -> {
                var params = rule.leakyBucket();
                response.setHeader("RateLimit-Limit", params.capacity() + ";w=" + params.interval().toSeconds());
                response.setHeader("X-RateLimit-Limit", String.valueOf(params.capacity()));
                response.setHeader("X-RateLimit-Policy",
                        "leaky-bucket; capacity=" + params.capacity() + "; interval=" + params.interval());
            }
            case Bucket4j -> {
                var params = rule.bucket4J();
                response.setHeader("RateLimit-Limit", params.capacity() + ";w=" + params.refill().toSeconds());
                response.setHeader("X-RateLimit-Limit", String.valueOf(params.capacity()));
                response.setHeader("X-RateLimit-Policy",
                        "bucket4j; capacity=" + params.capacity() + "; intervally=" + params.intervally() +
                        "; refill=" + params.refill() + "; tokens=" + params.tokens());
            }
        }
    }

    public void handleGranted(
            HttpServletResponse response, ResourceRateLimiter.Decision<String> decision
    ) {
        setBaseRateLimitHeaders(response, decision);
    }

    public void handleDenied(
            HttpServletResponse response, ResourceRateLimiter.Decision<String> decision
    ) throws IOException {
        Duration retryAfter = decision.retryAfter().orElse(props.defaultRetryAfter());
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        long delaySeconds = retryAfter.toSeconds();
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(delaySeconds));
        setBaseRateLimitHeaders(response, decision);
        long resetEpochSeconds = clock.instant().getEpochSecond() + delaySeconds;
        response.setHeader("RateLimit-Reset", String.valueOf(delaySeconds));
        response.setHeader("X-RateLimit-Reset", String.valueOf(resetEpochSeconds));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(decision.rule().errorJson());
    }

}
