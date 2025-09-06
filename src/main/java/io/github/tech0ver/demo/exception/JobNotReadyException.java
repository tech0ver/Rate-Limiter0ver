package io.github.tech0ver.demo.exception;

public class JobNotReadyException extends Exception {

    public JobNotReadyException(long jobId) {
        super("Job isn't processed: " + jobId);
    }

}
