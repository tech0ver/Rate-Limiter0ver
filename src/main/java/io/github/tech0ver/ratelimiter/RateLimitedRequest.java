package io.github.tech0ver.demo.ratelimiter;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Optional;

public interface RateLimitedRequest {

    static RateLimitedRequest from(HttpServletRequest request) {
        return new RateLimitedRequest() {

            @Override
            public Optional<String> pattern() {
                String pattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
                return Optional.ofNullable(pattern);
            }

            @Override
            public String url() {
                return request.getRequestURI();
            }

            @Override
            public String method() {
                return request.getMethod();
            }

            @Override
            public String ip() {
                return request.getRemoteAddr();
            }

            @Override
            public Optional<String> header(String header) {
                return Optional.ofNullable(request.getHeader(header));
            }

        };
    }

    Optional<String> pattern();

    String url();

    String method();

    String ip();

    Optional<String> header(String header);

}
