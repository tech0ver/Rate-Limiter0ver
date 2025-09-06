package io.github.tech0ver.demo.exporter;

import io.github.tech0ver.demo.domain.Event;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

@Component
public class CsvEventFileExporter implements EventFileExporter {

    private final Path exportDirectory = createExportDirectory();

    private Path createExportDirectory() {
        try {
            Path dir = Path.of(System.getProperty("java.io.tmpdir"), "exports", "csv");
            Files.createDirectories(dir);
            return dir;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot init export directory", e);
        }
    }

    @Override
    public String getMediaType() {
        return "text/csv; charset=utf-8";
    }

    @Override
    public Path exportFile(String filename, List<Event> events) throws IOException {
        String baseName = filename + ".csv";
        Path file = exportDirectory.resolve(baseName);
        CSVFormat fmt = CSVFormat.DEFAULT.builder()
                .setHeader("value", "createdAt")
                .setRecordSeparator('\n')
                .build();
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
             CSVPrinter printer = new CSVPrinter(writer, fmt)
        ) {
            for (Event event : events) {
                printer.printRecord(
                        event.value() == null ? "" : event.value(),
                        event.createdAt() == null ? "" : event.createdAt().toString()
                );
            }
        }
        return file;
    }

}
