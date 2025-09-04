package io.github.tech0ver.demo.domain;

import java.time.Instant;

public record Event(
        String value,
        Instant createdAt
) {
}
