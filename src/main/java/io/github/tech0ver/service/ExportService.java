package io.github.tech0ver.service;

import io.github.tech0ver.domain.ExportJob;
import io.github.tech0ver.exception.JobFailedException;
import io.github.tech0ver.exception.JobNotReadyException;
import io.github.tech0ver.exception.JobNotFoundException;

public interface ExportService {

    long createJob(String apiKey);

    ExportJob.Snapshot getJobSnapshot(String apiKey, long jobId) throws JobNotFoundException;

    ExportJob.File getJobFile(String apiKey, long jobId) throws JobNotReadyException, JobFailedException, JobNotFoundException;

}
