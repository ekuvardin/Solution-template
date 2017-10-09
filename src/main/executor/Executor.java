package main.executor;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.concurrent.*;

public class Executor {

    private final ConcurrentLinkedQueue<Runnable> tasks;
    private final ChainingThread workingThread = new ChainingThread();
    private final StateMachine stateMachine = new StateMachine();

    public Executor(int workers) {
        if (workers <= 0)
            throw new RuntimeException("Count of workers must be greater than zero");

        tasks = new ConcurrentLinkedQueue<>();

        final Runnable mainRun = () -> {
            workingThread.add();
            try {
                for (IExecutionStrategy strategy = stateMachine.getCurrentState(); strategy.process(tasks, stateMachine); strategy = stateMachine.getCurrentState()) {
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
        State state = stateMachine.getCurrentState();
        if (state == State.RUNNING) {
            tasks.offer(r);
            State.RUNNING.unParkThread();
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
        stateMachine.changeState(State.EXECUTE_ALL_TASK_THEN_TERMINATE);
    }

    public synchronized void shutdownNow() {
        stateMachine.changeState(State.TERMINATE);
        for (Thread thread : workingThread.workingThreads()) {
            if (thread != null) {
                thread.interrupt();
            }
        }
    }
}

