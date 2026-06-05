package io.github.tech0ver.demo.ratelimiter.web;

import io.github.tech0ver.demo.ratelimiter.RateLimitedRequest;
import io.github.tech0ver.demo.ratelimiter.registry.ResourceRateLimiterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final ResourceRateLimiterRegistry<RateLimitedRequest, String> registry;
    private final RateLimitResponseHandler responseHandler;

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler
    ) throws Exception {
        var optionalDecision = registry.tryPermit(RateLimitedRequest.from(request));
        if (optionalDecision.isEmpty()) return true;
        var decision = optionalDecision.get();
        if (decision.granted()){
            responseHandler.handleGranted(response, decision);
            return true;
        }
        responseHandler.handleDenied(response, decision);
        return false;
    }

}
