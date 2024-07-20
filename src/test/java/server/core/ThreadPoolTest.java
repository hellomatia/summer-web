package server.core;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ThreadPoolTest {
    @Test
    void 스레드풀이_태스크를_실행함() throws InterruptedException {
        ThreadPool threadPool = new ThreadPool(2);
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(5);

        for (int i = 0; i < 5; i++) {
            threadPool.execute(() -> {
                counter.incrementAndGet();
                latch.countDown();
            });
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(5, counter.get());
        threadPool.shutdown();
    }

    @Test
    void 스레드풀이_정상적으로_종료됨() throws InterruptedException {
        ThreadPool threadPool = new ThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);

        threadPool.execute(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            latch.countDown();
        });

        threadPool.shutdown();
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    void 스레드풀이_인터럽트_발생시_강제_종료됨() throws InterruptedException {
        ThreadPool threadPool = new ThreadPool(1);
        CountDownLatch latch = new CountDownLatch(1);

        Thread shutdownThread = new Thread(() -> {
            threadPool.shutdown();
            latch.countDown();
        });

        threadPool.execute(() -> {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        shutdownThread.start();
        shutdownThread.interrupt();

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
}
