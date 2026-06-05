package io.github.tech0ver.ratelimiter;

import java.util.concurrent.locks.LockSupport;

public interface ThreadParker {

    void park(long nanosToPark);

    void parkInterruptibly(long nanosToPark) throws InterruptedException;

    ThreadParker DEFAULT = new ThreadParker() {

        @Override
        public void park(long nanosToPark) {
            boolean interrupted = false;
            long endNanos = System.nanoTime() + nanosToPark;
            long remainingNanos = nanosToPark;
            try {
                while (true) {
                    LockSupport.parkNanos(remainingNanos);
                    if (Thread.interrupted()) interrupted = true;
                    remainingNanos = endNanos - System.nanoTime();
                    if (remainingNanos <= 0) return;
                }
            } finally {
                if (interrupted) Thread.currentThread().interrupt();
            }
        }

        @Override
        public void parkInterruptibly(long nanosToPark) throws InterruptedException {
            long endNanos = System.nanoTime() + nanosToPark;
            long remainingNanos = nanosToPark;
            while (true) {
                LockSupport.parkNanos(remainingNanos);
                if (Thread.interrupted()) throw new InterruptedException();
                remainingNanos = endNanos - System.nanoTime();
                if (remainingNanos <= 0) return;
            }
        }

        @Override public String toString() { return "DefaultThreadParker"; }

    };

    ThreadParker NO_OP = new ThreadParker() {

        @Override public void park(long nanosToPark) {}

        @Override public void parkInterruptibly(long nanosToPark) {}

        @Override public String toString() { return "NoOpThreadParker"; }

    };

}
