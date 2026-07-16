package io.github.tech0ver.config;

import io.github.tech0ver.config.props.RateLimitProps;
import io.github.tech0ver.ratelimiter.web.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(RateLimitProps.class)
public class RateLimitConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry interceptorRegistry) {
        interceptorRegistry
                .addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**");
    }

}
