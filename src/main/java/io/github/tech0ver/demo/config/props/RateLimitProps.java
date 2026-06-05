package io.github.tech0ver.demo.config.props;

import io.github.tech0ver.demo.ratelimiter.RateLimitRule;
import jakarta.validation.Valid;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = RateLimitProps.PREFIX)
public record RateLimitProps(
        @Valid List<RateLimitRule> rules,
        @DefaultValue("PT180S") Duration defaultRetryAfter
) {

    public static final String PREFIX = "rate-limit";

}
