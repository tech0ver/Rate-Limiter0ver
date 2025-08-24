package io.github.tech0ver.ratelimiter;

public interface MyWatch {

    MyWatch SYSTEM_NANO_TIME = System::nanoTime;

    long currentTimeNanos();

}
