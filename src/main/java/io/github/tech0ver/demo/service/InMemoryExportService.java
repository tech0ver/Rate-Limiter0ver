package io.github.tech0ver.demo.service;

import io.github.tech0ver.demo.domain.Event;
import io.github.tech0ver.demo.domain.EventSearchCondition;
import io.github.tech0ver.demo.domain.ExportJob;
import io.github.tech0ver.demo.exception.JobFailedException;
import io.github.tech0ver.demo.exception.JobNotFoundException;
import io.github.tech0ver.demo.exception.JobNotReadyException;
import io.github.tech0ver.demo.exporter.EventFileExporter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class InMemoryExportService implements ExportService {

    private final EventService eventService;
    private final EventFileExporter eventFileExporter;

    private final Clock clock;
    private final Executor executor;

    private final Map<String, State> stateByApiKey = new ConcurrentHashMap<>();
    private record State(Map<Long, ExportJob> jobs, AtomicLong counter) {}

    @Override
    public long createJob(String apiKey) {
        State state = stateByApiKey.computeIfAbsent(apiKey, k -> new State(new ConcurrentHashMap<>(), new AtomicLong()));
        long jobId = state.counter.getAndIncrement();
        ExportJob job = new ExportJob(jobId, clock.instant());
        state.jobs.put(jobId, job);
        executor.execute(() -> processJob(apiKey, job));
        return jobId;
    }

    public void processJob(String apiKey, ExportJob job) {
        EventSearchCondition condition = new EventSearchCondition(null, job.getCreatedAt());
        try {
            List<Event> events = eventService.search(apiKey, condition);
            try {
                job.getLock().writeLock().lockInterruptibly();
                job.setStatus(ExportJob.Status.PROCESSING);
                job.setMessage("Started processing events: " + events.size());
                job.setModifiedAt(clock.instant());
            } finally {
                job.getLock().writeLock().unlock();
            }
            Path file = eventFileExporter.exportFile("job-" + job.getId(), events);
            try {
                job.getLock().writeLock().lockInterruptibly();
                job.setStatus(ExportJob.Status.PROCESSED);
                job.setMessage("Processed events: " + events.size());
                job.setMediaType(eventFileExporter.getMediaType());
                job.setFile(file);
                long size;
                try {
                    size = Files.size(file);
                } catch (IOException e) {
                    size = 0;
                }
                job.setSize(size);
                job.setModifiedAt(clock.instant());
            } finally {
                job.getLock().writeLock().unlock();
            }
        } catch (Exception e) {
            try {
                job.getLock().writeLock().lock();
                job.setStatus(ExportJob.Status.FAILED);
                job.setMessage("Failed processing events: " + e.getMessage());
                job.setModifiedAt(clock.instant());
            } finally {
                job.getLock().writeLock().unlock();
            }
        }
    }

    private ExportJob getJob(String apiKey, long jobId) throws JobNotFoundException {
        ExportJob job = null;
        State state = stateByApiKey.get(apiKey);
        if (state != null) {
            Map<Long, ExportJob> jobs = state.jobs;
            if (jobs != null) job = jobs.get(jobId);
        }
        if (job == null) throw new JobNotFoundException(jobId);
        return job;
    }

    @Override
    public ExportJob.Snapshot getJobSnapshot(String apiKey, long jobId) throws JobNotFoundException {
        ExportJob job = getJob(apiKey, jobId);
        try {
            job.getLock().readLock().lock();
            return job.asSnapshot();
        } finally {
            job.getLock().readLock().unlock();
        }
    }

    @Override
    public ExportJob.File getJobFile(
            String apiKey, long jobId
    ) throws JobNotReadyException, JobFailedException, JobNotFoundException {
        ExportJob job = getJob(apiKey, jobId);
        try {
            job.getLock().readLock().lock();
            ExportJob.Status status = job.getStatus();
            switch (status) {
                case CREATED, PROCESSING -> throw new JobNotReadyException(jobId);
                case FAILED -> throw new JobFailedException(jobId);
            }
            return job.asFile();
        } finally {
            job.getLock().readLock().unlock();
        }
    }

}
