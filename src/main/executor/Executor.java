package main.executor;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.*;

public class Executor {

    private final ConcurrentLinkedQueue<Runnable> tasks;
    private final ChainingThread workingThread = new ChainingThread();

    @Nonnull
    private volatile IExecutionStrategy curStrategy = new SpinStrategy();

    public Executor(int workers) {
        if (workers <= 0)
            throw new RuntimeException("Count of workers must be greater than zero");

        tasks = new ConcurrentLinkedQueue<>();

        final Runnable mainRun = () -> {
            workingThread.add();
            try {
                for (IExecutionStrategy strategy = curStrategy; strategy.process(tasks); strategy = curStrategy) {
                }
            } finally {
                workingThread.remove();
            }
        };

        for (int i = 0; i < workers; i++) {
            Thread thread = new Thread(mainRun);
            thread.start();
        }
    }

    public void submit(@Nonnull Runnable r) {
        IExecutionStrategy locStrategy = curStrategy;
        if (locStrategy instanceof SpinStrategy) {
            tasks.offer(r);
            ((SpinStrategy) locStrategy).unParkThread();
        } else {
            throw new RejectedExecutionException("Submit tasks are forbidden");
        }
    }

    public synchronized boolean awaitTermination(long seconds) {
        changeState(new SoftShutdown());
        workingThread.waitEmpty(seconds);
        return workingThread.isEmpty();
    }

    public Iterator<Runnable> getRemainingTasks() {
        return tasks.iterator();
    }

    public synchronized void shutdown() {
        changeState(new EndExecution());
    }

    public synchronized void shutdownNow() {
        changeState(new EndExecution());
        for (Thread thread : workingThread.workingThreads()) {
            if (thread != null) {
                thread.interrupt();
            }
        }
    }

    private void changeState(@Nonnull IExecutionStrategy strategy) {
        IExecutionStrategy locStrategy = curStrategy;
        curStrategy = strategy;
        if (locStrategy instanceof SpinStrategy) {
            ((SpinStrategy) locStrategy).unParkAllThread();
        }
    }

    @FunctionalInterface
    interface IExecutionStrategy {

        boolean process(Queue<Runnable> tasks);

    }

    class EndExecution implements IExecutionStrategy {

        @Override
        public boolean process(Queue<Runnable> tasks) {
            return false;
        }
    }

    class SoftShutdown implements IExecutionStrategy {

        @Override
        public boolean process(Queue<Runnable> tasks) {
            for (Runnable r = tasks.poll(); curStrategy == this && r != null; r = tasks.poll()) {
                r.run();
                Thread.yield();
            }

            return false;
        }
    }

    class SpinStrategy implements IExecutionStrategy {

        private final int spinCount = 100;
        private final Object lock = new Object();

        @Override
        public boolean process(Queue<Runnable> tasks) {
            boolean res = true;
            do {
                Runnable r = tasks.poll();

                // simple spin
                for (int i = 0; i < spinCount && curStrategy == this && r == null; i++) {
                    Thread.yield();
                    r = tasks.poll();
                }

                // found task
                if (curStrategy == this && r != null)
                    r.run();

            } while (curStrategy == this && !(res = parkThread(tasks)));// park thread when spin too long

            return res;
        }

        private boolean parkThread(Queue<Runnable> tasks) {
            synchronized (lock) {
                if (tasks.isEmpty() && curStrategy == this) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        return false;
                    }
                }
            }

            return true;
        }

        private void unParkThread() {
            synchronized (lock) {
                lock.notify();
            }
        }

        private void unParkAllThread() {
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }
}

