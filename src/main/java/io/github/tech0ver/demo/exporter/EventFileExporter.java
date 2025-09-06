package io.github.tech0ver.demo.exporter;

import io.github.tech0ver.demo.domain.Event;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface EventFileExporter {

    String getMediaType();

    Path exportFile(String filename, List<Event> events) throws IOException;

}
