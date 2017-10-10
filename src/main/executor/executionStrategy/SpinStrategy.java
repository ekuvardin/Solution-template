package main.executor.executionStrategy;

import java.util.Queue;

/**
 * Strategy xecuting until call stopExecution and while new task are allowed to enter
 */
public class SpinStrategy implements IExecutionStrategy {

    private final int spinCount = 100;
    private final Object lock = new Object();
    private volatile boolean stop = false;

    @Override
    public boolean process(Queue<Runnable> tasks) {
        try {
            do {
                Runnable r = tasks.poll();

                // simple spin
                for (int i = 0; i < spinCount && !stop && r == null; i++) {
                    Thread.yield();
                    r = tasks.poll();
                }

                // found task
                if (!stop && r != null)
                    r.run();

                // park thread when spin too long
                parkThread(tasks);
            }
            while (!stop);
        } catch (InterruptedException e) {
            return false;
        }

        return true;
    }

    @Override
    public void stopExecution() {
        stop = true;

        // Wakeup all thread to soft proceed and exit executing current strategy
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    @Override
    public void onTrySubmitTask() {
        synchronized (lock) {
            lock.notify();
        }
    }

    @Override
    public boolean canSubmitTask() {
        return true;
    }

    private void parkThread(Queue<Runnable> tasks) throws InterruptedException {
        synchronized (lock) {
            if (tasks.isEmpty() && !stop) {
                lock.wait();
            }
        }
    }
}
