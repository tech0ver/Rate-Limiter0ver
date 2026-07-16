package io.github.tech0ver.domain;

import java.time.Instant;

public record Event(
        String value,
        Instant createdAt
) {
}
