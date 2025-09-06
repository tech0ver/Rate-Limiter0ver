package io.github.tech0ver.demo.service;

import io.github.tech0ver.demo.domain.ExportJob;
import io.github.tech0ver.demo.exception.JobFailedException;
import io.github.tech0ver.demo.exception.JobNotReadyException;
import io.github.tech0ver.demo.exception.JobNotFoundException;

public interface ExportService {

    long createJob();

    ExportJob.Snapshot getJobSnapshot(long jobId) throws JobNotFoundException;

    ExportJob.File getJobFile(long jobId) throws JobNotReadyException, JobFailedException, JobNotFoundException;

}
