package course.concurrency.exams.refactoring;

import java.util.concurrent.ThreadFactory;

public class MountTableRefresherThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(Runnable r) {
        var thread = new Thread(r);
        thread.setName("MountTableRefresh_%d");
        thread.setDaemon(true);
        return thread;
    }
}
