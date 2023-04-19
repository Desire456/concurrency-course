package course.concurrency.queue;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlockingQueueTest {

    private static final int CAPACITY = 5;

    @Test
    void shouldWaitIfMaxCapacityIsReached() {
        var queue = new MyBlockingQueue<Integer>(CAPACITY);

        var thread = new Thread(() -> {
            for (int i = 0; i < CAPACITY + 1; ++i) {
                try {
                    queue.enqueue(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();

        awaitUntilThreadStateIs(thread, Thread.State.WAITING);
        assertEquals(CAPACITY, queue.getSize());
    }

    @Test
    void shouldWaitIfZeroElements() {
        var queue = new MyBlockingQueue<Integer>(CAPACITY);

        var thread = new Thread(() -> {
            try {
                queue.dequeue();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();

        awaitUntilThreadStateIs(thread, Thread.State.WAITING);
        assertEquals(0, queue.getSize());
    }

    @Test
    void shouldContinueAfterDequeue() {
        var queue = new MyBlockingQueue<Integer>(CAPACITY);
        var enqThread = new Thread(() -> {
            for (int i = 0; i < CAPACITY + 1; ++i) {
                try {
                    queue.enqueue(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        enqThread.start();

        try {
            assertEquals(1, queue.dequeue());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        awaitUntilThreadStateIs(enqThread, Thread.State.TERMINATED);
    }

    @Test
    void shouldContinueAfterEnqueue() {
        var queue = new MyBlockingQueue<Integer>(CAPACITY);
        var deqThread = new Thread(() -> {
            try {
                assertEquals(1, queue.dequeue());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        deqThread.start();

        try {
            queue.enqueue(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        awaitUntilThreadStateIs(deqThread, Thread.State.TERMINATED);
    }

    @Test
    void shouldBeFIFO() throws InterruptedException {
        var queue = new MyBlockingQueue<Integer>(CAPACITY);

        queue.enqueue(1);
        queue.enqueue(2);
        queue.enqueue(3);

        assertEquals(3, queue.getSize());

        assertEquals(1, queue.dequeue());
        assertEquals(2, queue.dequeue());
        assertEquals(3, queue.dequeue());

        assertEquals(0, queue.getSize());
    }

    @Test
    void sequentialUse() throws InterruptedException {
        var queue = new MyBlockingQueue<Integer>(CAPACITY);

        assertEquals(0, queue.getSize());

        queue.enqueue(123);
        assertEquals(1, queue.getSize());

        assertEquals(123, queue.dequeue());
        assertEquals(0, queue.getSize());
    }

    @Test
    void intensiveWork() throws InterruptedException {
        var iterations = 1_000_000;
        var workers = 5;
        var queue = new MyBlockingQueue<Integer>(workers * iterations);

        var countDownLatch = new CountDownLatch(1);
        var executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < workers; i++) {
            executorService.submit(() -> {
                try {
                    countDownLatch.await();
                    for (int j = 0; j < iterations; j++) {
                        queue.enqueue(j);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            executorService.submit(() -> {
                try {
                    countDownLatch.await();
                    for (int j = 0; j < iterations; j++) {
                        queue.dequeue();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        countDownLatch.countDown();

        executorService.shutdown();
        int waitTimeInSec = 5;
        assertTrue(executorService.awaitTermination(waitTimeInSec, TimeUnit.SECONDS),
                "Doesn't finished for " + waitTimeInSec + " seconds");
        assertEquals(0, queue.getSize());
    }

    private static void awaitUntilThreadStateIs(Thread deqThread, Thread.State terminated) {
        await().atMost(1, TimeUnit.SECONDS).until(() -> deqThread.getState() == terminated);
    }
}
