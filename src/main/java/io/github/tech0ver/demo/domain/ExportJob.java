package io.github.tech0ver.demo.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ExportJob {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @EqualsAndHashCode.Include
    private final long id;
    private final Instant createdAt;

    private Instant modifiedAt;
    private Status status = ExportJob.Status.CREATED;
    private String message;
    private String mediaType;
    private Path file;
    private long size;

    public Snapshot asSnapshot() {
        return new Snapshot(
                id,
                createdAt,
                modifiedAt,
                status,
                message
        );
    }

    public File asFile() {
        return new File(
                file,
                mediaType,
                modifiedAt,
                size
        );
    }

    public enum Status {CREATED, PROCESSING, FAILED, PROCESSED}

    public record Snapshot(
            long id,
            Instant createdAt,
            Instant modifiedAt,
            Status status,
            String message
    ) {
    }

    public record File(
            Path path,
            String mediaType,
            Instant modifiedAt,
            long size
    ) {
    }

}
