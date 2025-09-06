package io.github.tech0ver.demo.exception;

public class JobFailedException extends Exception {

    public JobFailedException(long jobId) {
        super("Job failed: " + jobId);
    }

}
