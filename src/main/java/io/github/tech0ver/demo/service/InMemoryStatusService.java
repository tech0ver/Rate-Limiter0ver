package io.github.tech0ver.demo.service;

import io.github.tech0ver.demo.domain.Status;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class InMemoryStatusService implements StatusService {

    private final Instant startedAt = Instant.now();

    @Override
    public Status getStatus() {
        Instant now = Instant.now();
        return new Status(
                now,
                Duration.between(startedAt, now).toSeconds()
        );
    }

}
