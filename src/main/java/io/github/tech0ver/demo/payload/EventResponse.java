package io.github.tech0ver.demo.payload;

import java.time.Instant;

public record EventResponse(
        String value,
        Instant createdAt
) {
}
