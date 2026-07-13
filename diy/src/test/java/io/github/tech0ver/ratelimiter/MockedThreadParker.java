package io.github.tech0ver.ratelimiter;

public class MockedThreadParker implements ThreadParker {

    @Override
    public void park(long nanosToPark) {
        System.out.println("Mocked: parking on " + nanosToPark + " nanos");
    }

    @Override
    public void parkInterruptibly(long nanosToPark) {
        System.out.println("Mocked: parking interruptibly on " + nanosToPark + " nanos");
    }

}
