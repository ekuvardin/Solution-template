package main.executor;

import main.executor.executionStrategy.IExecutionStrategy;
import main.executor.states.State;
import main.executor.states.StateMachine;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simple executor which starts several threads and stop on shutdown/shutdownNow
 */
public class Executor {

    /**
     * Queue of executing tasks
     */
    private final LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();


    /**
     * List of working threads on tasks
     */
    private final ChainingThread workingThread = new ChainingThread();


    /**
     * Link chain between State and IExecutionStrategy
     */
    private final StateMachine stateMachine = new StateMachine();

    /**
     * Main lock to control changing Executor states
     */
    private final ReentrantLock main = new ReentrantLock();

    public Executor(int workers) {
        if (workers <= 0)
            throw new RuntimeException("Count of workers must be greater than zero");

        final Runnable mainRun = new MainRunner();

        for (int i = 0; i < workers; i++) {
            ChainingThread.Worker thread = new ChainingThread.Worker(mainRun, this.workingThread);
            thread.start();
        }
    }

    /**
     * Try submit task. If submitting tasks are forbidden then throw RejectedExecutionException
     *
     * @param r submitting runnable
     */
    public void submit(@Nonnull Runnable r) {
        IExecutionStrategy strategy = stateMachine.getCurrentStrategy();
        if (strategy.canSubmitTask()) {
            tasks.add(r);
            strategy.onTrySubmitTask();
        } else {
            throw new RejectedExecutionException("Submitting task are forbidden");
        }
    }

    /**
     * Shutdown Executor and wait for specified time to it termination
     *
     * @param seconds wait in seconds
     * @return true - if all threads aren't active, false -other
     */
    public boolean awaitTermination(long seconds) {
        shutdown();
        workingThread.waitEmpty(seconds);
        return workingThread.isEmpty();
    }

    /**
     * Get remaining task to be executed
     *
     * @return remaining task
     */
    public Iterator<Runnable> getRemainingTasks() {
        return tasks.iterator();
    }

    /**
     * Execute all tasks, rejected new and shutdown
     */
    public void shutdown() {
        main.lock();
        try {
            stateMachine.changeState(State.EXECUTE_ALL_TASK_THEN_TERMINATE);
        } finally {
            main.unlock();
        }
    }

    /**
     * Shutdown all running threads immediately
     */
    public void shutdownNow() {
        main.lock();
        try {
            stateMachine.changeState(State.TERMINATE);
            for (Thread thread : workingThread.workingThreads()) {
                if (thread != null) {
                    thread.interrupt();
                }
            }
        } finally {
            main.unlock();
        }
    }

    /**
     * All working threads run within this class and do actions according to current IExecutionStrategy
     */
    class MainRunner implements Runnable {

        @Override
        public void run() {
            for (IExecutionStrategy strategy = stateMachine.getCurrentStrategy(); strategy.process(tasks); strategy = stateMachine.getCurrentStrategy()) {
                Thread.yield();
            }
        }
    }

}

