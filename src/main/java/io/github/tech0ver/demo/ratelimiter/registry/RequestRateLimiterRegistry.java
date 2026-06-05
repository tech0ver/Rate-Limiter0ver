package io.github.tech0ver.demo.ratelimiter.registry;

import io.github.tech0ver.demo.ratelimiter.RateLimitedRequest;
import io.github.tech0ver.demo.ratelimiter.ResourceRateLimiter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RequestRateLimiterRegistry implements ResourceRateLimiterRegistry<RateLimitedRequest, String> {

    private static final String ANY_METHOD = "*";

    private final Map<String, Map<String, ResourceRateLimiter<RateLimitedRequest, String>>> limiterByPattern;

    public RequestRateLimiterRegistry(List<ResourceRateLimiter<RateLimitedRequest, String>> limiters) {
        this.limiterByPattern = new ConcurrentHashMap<>();
        limiters.forEach(this::register);
    }

    @Override
    public void register(ResourceRateLimiter<RateLimitedRequest, String> rateLimiter) {
        requireNewLimiter(rateLimiter);
        var rule = rateLimiter.rule();
        var limiterByMethod = limiterByPattern.computeIfAbsent(rule.pattern(), k -> new ConcurrentHashMap<>());
        var methods = rule.methodNames();
        if (methods.isEmpty()) limiterByMethod.put(ANY_METHOD, rateLimiter);
        else for (String method : methods) limiterByMethod.put(method, rateLimiter);
    }

    private void requireNewLimiter(ResourceRateLimiter<RateLimitedRequest, String> newLimiter) {
        var rule = newLimiter.rule();
        var limiterByMethod = limiterByPattern.get(rule.pattern());
        if (limiterByMethod == null) return;
        var methods = rule.methodNames();
        if (methods.isEmpty()) {
            var limiter = limiterByMethod.get(ANY_METHOD);
            if (limiter == null) return;
            throw new IllegalArgumentException(
                    "Registry already has the limiter with the same rule for all methods: " + limiter
            );
        }
        for (String method : methods) {
            var limiter = limiterByMethod.get(method);
            if (limiter == null) continue;
            throw new IllegalArgumentException(
                    "Registry already has the limiter with the same rule for '" + method + "': " + limiter
            );
        }
    }

    @Override
    public Optional<ResourceRateLimiter.Decision<String>> tryPermit(RateLimitedRequest request) {
        String pattern = request.pattern().orElseGet(request::url);
        var limiterByMethod = limiterByPattern.get(pattern);
        if (limiterByMethod == null) return Optional.empty();
        String method = request.method();
        var limiter = limiterByMethod.get(method);
        if (limiter == null) limiter = limiterByMethod.get(ANY_METHOD);
        if (limiter == null) return Optional.empty();
        var decision = limiter.tryPermit(request);
        return Optional.ofNullable(decision);
    }

}
