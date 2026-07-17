package io.github.tech0ver.exporter;

import io.github.tech0ver.domain.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class SlowEventFileExporter implements EventFileExporter {

    private final EventFileExporter delegate;

    @Value("${export.sleep-timeout:5000}")
    private long timeout;

    @Override
    public String getMediaType() {
        return delegate.getMediaType();
    }

    @Override
    public Path exportFile(String filename, List<Event> events) throws IOException {
        try {
            TimeUnit.MILLISECONDS.sleep(timeout);
        } catch (InterruptedException ignored) {
        }
        return delegate.exportFile(filename, events);
    }

}
