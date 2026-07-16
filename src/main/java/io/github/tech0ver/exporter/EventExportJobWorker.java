package io.github.tech0ver.exporter;

import io.github.tech0ver.domain.ExportJob;

public interface EventExportJobWorker {

    void processJob(String apiKey, ExportJob job);

}
