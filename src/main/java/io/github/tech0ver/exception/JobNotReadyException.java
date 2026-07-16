package io.github.tech0ver.exception;

public class JobNotReadyException extends Exception {

    public JobNotReadyException(long jobId) {
        super("Job isn't processed: " + jobId);
    }

}
