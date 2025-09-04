package io.github.tech0ver.demo.domain;

import org.springframework.lang.Nullable;

import java.time.Instant;

public record EventSearchCondition(
        @Nullable Instant from,
        @Nullable Instant to
) {
}
