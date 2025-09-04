package io.github.tech0ver.demo.domain;

import java.time.Instant;

public record Status(
        Instant now,
        long uptimeSeconds
) {
}
