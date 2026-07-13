package io.github.tech0ver.demo.payload.v1;

import java.time.Instant;

public record StatusResponse(
        Instant now,
        long uptimeSeconds
) {
}
