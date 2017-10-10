package main.executor.executionStrategy;

import java.util.Queue;

/**
 * Strategy executing while new tasks exists and new tasks are rejected
 */
public class SoftShutdown implements IExecutionStrategy {

    private volatile boolean stop = false;

    @Override
    public boolean process(Queue<Runnable> tasks) {
        for (Runnable r = tasks.poll(); !stop && r != null; r = tasks.poll()) {
            r.run();
            Thread.yield();
        }

        return false;
    }

    @Override
    public void stopExecution() {
        stop = true;
    }

    @Override
    public void onTrySubmitTask() {
        throw new RuntimeException("Submit new tasks are forbidden");
    }

    @Override
    public boolean canSubmitTask() {
        return false;
    }
}
