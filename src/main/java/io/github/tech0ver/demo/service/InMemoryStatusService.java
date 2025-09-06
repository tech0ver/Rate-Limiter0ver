package io.github.tech0ver.demo.service;

import io.github.tech0ver.demo.domain.Status;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class InMemoryStatusService implements StatusService {

    private final Clock clock;
    private final Instant startedAt;

    public InMemoryStatusService(Clock clock) {
        this.clock = clock;
        this.startedAt = clock.instant();
    }

    @Override
    public Status getStatus() {
        Instant now = clock.instant();
        return new Status(
                now,
                Duration.between(startedAt, now).toSeconds()
        );
    }

}
