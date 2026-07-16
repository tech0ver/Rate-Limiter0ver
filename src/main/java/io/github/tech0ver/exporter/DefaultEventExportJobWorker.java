package io.github.tech0ver.exporter;

import io.github.tech0ver.domain.Event;
import io.github.tech0ver.domain.EventSearchCondition;
import io.github.tech0ver.domain.ExportJob;
import io.github.tech0ver.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DefaultEventExportJobWorker implements EventExportJobWorker {

    private final EventService eventService;
    private final EventFileExporter eventFileExporter;

    private final Clock clock;

    @Override
    public void processJob(String apiKey, ExportJob job) {
        try {
            var events = searchEvents(apiKey, job);
            Path file = eventFileExporter.exportFile("job-" + job.getId(), events);
            addResult(job, events, file);
        } catch (Exception e) {
            addFailure(job, e);
        }
    }

    private List<Event> searchEvents(String apiKey, ExportJob job) throws InterruptedException {
        EventSearchCondition condition = new EventSearchCondition(null, job.getCreatedAt());
        List<Event> events = eventService.search(apiKey, condition);
        try {
            job.getLock().writeLock().lockInterruptibly();
            job.setStatus(ExportJob.Status.PROCESSING);
            job.setMessage("Started processing events: " + events.size());
            job.setModifiedAt(clock.instant());
        } finally {
            job.getLock().writeLock().unlock();
        }
        return events;
    }

    private void addResult(ExportJob job, List<Event> events, Path file) throws InterruptedException {
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
    }

    private void addFailure(ExportJob job, Exception e) {
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
