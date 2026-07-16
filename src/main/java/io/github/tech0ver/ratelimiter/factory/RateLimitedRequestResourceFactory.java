package io.github.tech0ver.ratelimiter.factory;

import io.github.tech0ver.ratelimiter.RateLimitRule;
import io.github.tech0ver.ratelimiter.RateLimitedRequest;
import org.springframework.stereotype.Component;

@Component
public class RateLimitedRequestResourceFactory<T> implements RateLimitedResourceFactory<RateLimitedRequest> {

    @Override
    public String createResourceByRule(RateLimitRule rule, RateLimitedRequest request) {
        return switch (rule.resourceType()) {
            case Ip -> request.ip();
            case Header -> request.header(rule.header())
                    .orElseThrow(() -> new IllegalArgumentException("No header: " + rule.header()));
        };
    }

}
