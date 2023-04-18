package course.concurrency.exams.refactoring;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class MountTableRefresherThreadFactory implements ThreadFactory {

    private final AtomicInteger threadNum = new AtomicInteger(0);

    @Override
    public Thread newThread(Runnable r) {
        var thread = new Thread(r);
        thread.setName("MountTableRefresh_" + threadNum.getAndIncrement());
        thread.setDaemon(true);
        return thread;
    }
}
