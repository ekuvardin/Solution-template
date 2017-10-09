package main.executor;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.*;

public class Executor {

    private final ConcurrentLinkedQueue<Runnable> tasks;
    private final ChainingThread workingThread = new ChainingThread();
    private final StateMachine stateMachine = new StateMachine();

    @Nonnull
    private volatile IExecutionStrategy curStrategy = new SpinStrategy();

    public Executor(int workers) {
        if (workers <= 0)
            throw new RuntimeException("Count of workers must be greater than zero");

        tasks = new ConcurrentLinkedQueue<>();

        final Runnable mainRun = () -> {
            workingThread.add();
            try {
                for(StateMachine.State curState = stateMachine.getCurrentState(); )
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
        shutdown();
        workingThread.waitEmpty(seconds);
        return workingThread.isEmpty();
    }

    public Iterator<Runnable> getRemainingTasks() {
        return tasks.iterator();
    }

    public synchronized void shutdown() {
        changeState(new SoftShutdown());
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


}

