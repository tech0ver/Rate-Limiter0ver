package io.github.tech0ver.demo.payload;

import java.time.Instant;

public record NewEventRequest(
        String value,
        Instant createdAt
) {
}
