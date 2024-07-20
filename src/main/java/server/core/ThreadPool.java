package server.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
    private final ExecutorService executor;

    public ThreadPool(int size) {
        this.executor = Executors.newFixedThreadPool(size);
    }

    public void execute(Runnable task) {
        executor.execute(task);
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
