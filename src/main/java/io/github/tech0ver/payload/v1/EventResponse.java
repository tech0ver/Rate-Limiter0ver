package io.github.tech0ver.payload.v1;

import java.time.Instant;

public record EventResponse(
        String value,
        Instant createdAt
) {
}
