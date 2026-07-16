package io.github.tech0ver.domain;

import java.time.Instant;

public record Status(
        Instant now,
        long uptimeSeconds
) {
}
