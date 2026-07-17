package io.github.tech0ver.service;

import io.github.tech0ver.domain.ExportJob;
import io.github.tech0ver.exception.JobFailedException;
import io.github.tech0ver.exception.JobNotFoundException;
import io.github.tech0ver.exception.JobNotReadyException;
import io.github.tech0ver.exporter.EventExportJobWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class InMemoryExportService implements ExportService {

    private final EventExportJobWorker eventExportJobWorker;

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
        executor.execute(() -> eventExportJobWorker.processJob(apiKey, job)); // naive
        return jobId;
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
