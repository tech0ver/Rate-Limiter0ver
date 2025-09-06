package io.github.tech0ver.demo.payload;

import java.time.Instant;

public record ExportJobResponse(
        long id,
        Instant createdAt,
        Instant modifiedAt,
        Status status,
        String message
) {

    public enum Status {CREATED, PROCESSING, FAILED, PROCESSED}

}
