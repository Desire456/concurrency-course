package course.concurrency.queue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

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
        awaitUntilThreadStateIs(enqThread, Thread.State.WAITING);

        new Thread(() -> {
            try {
                Assertions.assertEquals(1, queue.dequeue());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        awaitUntilThreadStateIs(enqThread, Thread.State.TERMINATED);
    }

    @Test
    void shouldContinueAfterEnqueue() {
        var queue = new MyBlockingQueue<Integer>(CAPACITY);
        var deqThread = new Thread(() -> {
            try {
                Assertions.assertEquals(1, queue.dequeue());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        deqThread.start();
        awaitUntilThreadStateIs(deqThread, Thread.State.WAITING);

        new Thread(() -> {
            try {
                queue.enqueue(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        awaitUntilThreadStateIs(deqThread, Thread.State.TERMINATED);
    }

    private static void awaitUntilThreadStateIs(Thread deqThread, Thread.State terminated) {
        await().atMost(1, TimeUnit.SECONDS).until(() -> deqThread.getState() == terminated);
    }
}
