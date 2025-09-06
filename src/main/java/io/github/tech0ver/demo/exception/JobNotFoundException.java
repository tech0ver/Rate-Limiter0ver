package io.github.tech0ver.demo.exception;

public class JobNotFoundException extends Exception {

    public JobNotFoundException(long jobId) {
        super("Job not found by id: " + jobId);
    }

}
