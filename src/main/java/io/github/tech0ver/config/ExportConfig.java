package io.github.tech0ver.config;

import io.github.tech0ver.exporter.CsvEventFileExporter;
import io.github.tech0ver.exporter.EventFileExporter;
import io.github.tech0ver.exporter.SlowEventFileExporter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExportConfig {

    @Bean
    public EventFileExporter csvEventFileExporter() {
        return new CsvEventFileExporter();
    }

    @Bean("defaultEventFileExporter")
    public EventFileExporter eventFileExporter(EventFileExporter eventFileExporter) {
        return new SlowEventFileExporter(eventFileExporter);
    }

}
