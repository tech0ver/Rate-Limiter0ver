package io.github.tech0ver.demo.payload;

import java.time.Instant;

public record StatusResponse(
        Instant now,
        long uptimeSeconds
) {
}
