package io.github.tech0ver.demo.service;

import io.github.tech0ver.demo.domain.ExportJob;
import io.github.tech0ver.demo.exception.JobFailedException;
import io.github.tech0ver.demo.exception.JobNotReadyException;
import io.github.tech0ver.demo.exception.JobNotFoundException;

public interface ExportService {

    long createJob(String apiKey);

    ExportJob.Snapshot getJobSnapshot(String apiKey, long jobId) throws JobNotFoundException;

    ExportJob.File getJobFile(String apiKey, long jobId) throws JobNotReadyException, JobFailedException, JobNotFoundException;

}
