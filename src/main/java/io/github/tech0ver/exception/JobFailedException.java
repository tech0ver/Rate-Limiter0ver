package io.github.tech0ver.exception;

public class JobFailedException extends Exception {

    public JobFailedException(long jobId) {
        super("Job failed: " + jobId);
    }

}
