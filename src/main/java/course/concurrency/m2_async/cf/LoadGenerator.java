package course.concurrency.m2_async.cf;

import java.util.stream.IntStream;

public class LoadGenerator {

    private static final int MAX_COMPUTATION_RANGE = 50_000_000;
    private static final long SLEEP_TIME = 1500;

    public static void work() {
        sleep();
//        compute();
    }

    private static void sleep() {
        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static int compute() {
        return IntStream.range(0, MAX_COMPUTATION_RANGE).boxed().filter(i -> i % 2 == 0).reduce((a, b) -> b).get();
    }
}