package course.concurrency.queue;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MyBlockingQueue<T> {

    private final int capacity;
    private int size;
    private Node<T> head;
    private Node<T> tail;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition isEmptyCondition = lock.newCondition();
    private final Condition isFullCondition = lock.newCondition();

    public MyBlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    public void enqueue(T value) throws InterruptedException {
        lock.lock();
        try {
            while (size == capacity) {
                isFullCondition.await();
            }

            if (head == null) {
                head = new Node<>(value);
                tail = head;
            } else if (tail == head) {
                tail = new Node<>(value);
                head.next = tail;
            } else {
                tail.next = new Node<>(value);
                tail = tail.next;
            }

            size++;
            isEmptyCondition.signal();
        } finally {
            lock.unlock();
        }
    }

    public T dequeue() throws InterruptedException {
        lock.lock();
        try {
            while (size == 0) {
                isEmptyCondition.await();
            }

            var value = head.item;
            head = head.next;
            size--;
            isFullCondition.signal();
            return value;
        } finally {
            lock.unlock();
        }
    }

    int getSize() {
        lock.lock();
        try {
            return size;
        } finally {
            lock.unlock();
        }
    }

    static class Node<T> {
        T item;

        Node<T> next;

        Node(T x) {
            item = x;
        }
    }
}
