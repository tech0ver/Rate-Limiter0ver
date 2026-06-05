package io.github.tech0ver.demo.config;

import io.github.tech0ver.ratelimiter.ThreadParker;
import io.github.tech0ver.ratelimiter.TimeSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class DiyRateLimiterConfig {

    @Bean
    public TimeSource monotonicTimeSource() {
        return TimeSource.MONOTONIC;
    }

    @Bean
    public ThreadParker defaultThreadParker() {
        return ThreadParker.DEFAULT;
    }

}
