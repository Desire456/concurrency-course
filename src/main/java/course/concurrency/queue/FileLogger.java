package course.concurrency.queue;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class FileLogger {

    private static final BufferedWriter WRITER;

    static {
        try {
            WRITER = new BufferedWriter(new FileWriter("1.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void log(String toLog) {
//        CompletableFuture.runAsync(() -> {
        try {
            synchronized (WRITER) {
//                System.out.println(toLog);
                WRITER.write(toLog + "\n");
                WRITER.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        });
    }
}
