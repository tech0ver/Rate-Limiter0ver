package io.github.tech0ver.ratelimiter;

public class MockedThreadParker implements ThreadParker {

    @Override
    public void park(long nanosToPark) {
        // do nothing
    }

    @Override
    public void parkInterruptibly(long nanosToPark) {
        // do nothing
    }

}
