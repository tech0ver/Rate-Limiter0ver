package io.github.tech0ver.demo.ratelimiter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.http.HttpMethod;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;

public record RateLimitRule(
        @DefaultValue("true") Boolean enabled,
        @NotBlank String pattern,
        Set<HttpMethod> methods,
        String header,
        Boolean ip,
        @DefaultValue("""
                {"error":"rate_limit_exceeded","message":"Too many requests."}
                """)
        String errorJson,
        @Valid FixedWindow fixedWindow,
        @Valid SlidingLog slidingLog,
        @Valid SlidingBuckets slidingBuckets,
        @Valid SlidingLinear slidingLinear,
        @Valid TokenBucket tokenBucket,
        @Valid LeakyBucket leakyBucket,
        @Valid Bucket4J bucket4J
) {

    public String asKey() {
        String key = limiterType() + ":" + pattern;
        Set<String> methodNames = methodNames();
        if (methodNames.isEmpty()) {
            return key;
        }
        return key + ":" + methodNames.stream()
                .sorted()
                .collect(Collectors.joining("/"));
    }

    public Set<String> methodNames() {
        if (methods == null) return Set.of();
        return methods.stream()
                .map(HttpMethod::name)
                .collect(Collectors.toSet());
    }

    @AssertTrue(message = "Exactly one resource must be set in a rule")
    public boolean isOneOfResource() {
        int count = 0;
        if (header != null) count++;
        if (Boolean.TRUE.equals(ip)) count++;
        return count == 1;
    }

    public enum ResourceType {
        Ip,
        Header,
    }

    public ResourceType resourceType() {
        if (header != null) return ResourceType.Header;
        if (Boolean.TRUE.equals(ip)) return ResourceType.Ip;
        throw new IllegalStateException("Exactly one resource must be set");
    }

    @AssertTrue(message = "Exactly one limiter block must be set in a rule")
    public boolean isOneOfLimiters() {
        int count = 0;
        if (fixedWindow != null) count++;
        if (slidingLog != null) count++;
        if (slidingBuckets != null) count++;
        if (slidingLinear != null) count++;
        if (tokenBucket != null) count++;
        if (leakyBucket != null) count++;
        if (bucket4J != null) count++;
        return count == 1;
    }

    public enum LimiterType {
        FixedWindow,
        SlidingLog,
        SlidingBuckets,
        SlidingLinear,
        TokenBucket,
        LeakyBucket,
        Bucket4j,
    }

    public LimiterType limiterType() {
        if (fixedWindow != null) return LimiterType.FixedWindow;
        if (slidingLog != null) return LimiterType.SlidingLog;
        if (slidingBuckets != null) return LimiterType.SlidingBuckets;
        if (slidingLinear != null) return LimiterType.SlidingLinear;
        if (tokenBucket != null) return LimiterType.TokenBucket;
        if (leakyBucket != null) return LimiterType.LeakyBucket;
        if (bucket4J != null) return LimiterType.Bucket4j;
        throw new IllegalStateException("Exactly one limiter block must be set");
    }

    public record FixedWindow(@NotNull Long limit, @NotNull Duration window) {}

    public record SlidingLog(@NotNull Integer limit, @NotNull Duration window) {}

    public record SlidingBuckets(@NotNull Long limit, @NotNull Duration window, @NotNull Integer buckets) {}

    public record SlidingLinear(@NotNull Long limit, @NotNull Duration window) {}

    public record TokenBucket(@NotNull Long capacity, @NotNull Duration refill, @DefaultValue("1") Long tokens) {}

    public record LeakyBucket(@NotNull Long capacity, @NotNull Duration interval) {}

    public record Bucket4J(
            @NotNull Long capacity, @DefaultValue("false") Boolean intervally,
            @NotNull Duration refill, @DefaultValue("1") Long tokens
    ) {}

}
