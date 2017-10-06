package main.executor;

import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

public class Executor {

    private final ConcurrentLinkedQueue<Runnable> tasks;
    private final BlockingQueue<Thread> parkedThreads;
    private final int spinCount = 100;
    private volatile ITaskStrategy taskStrategy;

    private volatile State curState = State.RUNNING;

    enum State {

        RUNNING,
        GRACEFULLY_TERMINATE,
        TERMINATE_AND_EXECUTE_ALL_TASK,
        SHUTDOWN;

        private ITaskStrategy strategy;

        public void doProcess(){
            this.strategy.process();
        }

        public void afterProcess(){
            this.strategy.afterProcess();
        }
    }



    private Runnable mainRun = new Runnable() {

        @Override
        public void run() {
            while(true){
                switch (curState){
                    case RUNNING: curState.doProcess(); break;
                    default :break;

                }
            }
            while (curState == State.RUNNING) {
                Runnable r = tasks.poll();

                for (int i = 0; i < spinCount && curState == State.RUNNING && r == null; i++) {
                    Thread.yield();
                    r = tasks.poll();
                }

                if (curState == State.RUNNING) {
                    if (r != null)
                        r.run();
                    else if (parkThread(Thread.currentThread()))
                        return;
                }
            }

            if (curState == State.TERMINATE_AND_EXECUTE_ALL_TASK) {
                Runnable r;
                while ((r = tasks.poll()) != null) {
                    r.run();
                }
            }
        }

    };

    private boolean parkThread(Thread thread) {
        if (tasks.isEmpty()) {
            synchronized (this) {
                if (tasks.isEmpty()) {
                    parkedThreads.add(thread);
                }
            }
            LockSupport.park(thread);
            return Thread.currentThread().isInterrupted();
        }

        return false;
    }

    private void unParkThread() {
        Thread thread = parkedThreads.poll();
        if (thread == null) {
            synchronized (this) {
                thread = parkedThreads.poll();
                if (thread == null) {
                    return;
                }
            }
        }
        LockSupport.unpark(thread);
    }

    public Executor(int workers) {
        tasks = new ConcurrentLinkedQueue<Runnable>();
        parkedThreads = new ArrayBlockingQueue<>(workers);

        for (int i = 0; i < workers; i++) {
            Thread thread = new Thread(mainRun);
            thread.start();
        }
    }

    public void submit(Runnable r) {
        if (curState == State.RUNNING) {
            tasks.offer(r);
            unParkThread();
        } else {
            throw new RejectedExecutionException("Submit tasks are forbidden");
        }
    }

    public void shutdownUntilAllTaskWereExecuted() {
        State oldState = curState;
        oldState.doProcess();
        curState = State.TERMINATE_AND_EXECUTE_ALL_TASK;
    }

    public void shutdownUntilAllTaskWereExecuted(long nanos, TimeUnit timeUnit) {
        curState = State.TERMINATE_AND_EXECUTE_ALL_TASK;

    }

    public void shutdown() {
        curState = State.GRACEFULLY_TERMINATE;
    }

    public void shutdownNow() {
        curState = State.SHUTDOWN;
    }

    interface ITaskStrategy {

        void process();

        void afterProcess();
    }


    class SpinStrategy implements ITaskStrategy {

        @Override
        public void process() {
            while (curState == State.RUNNING) {
                Runnable r = tasks.poll();

                for (int i = 0; i < spinCount && curState == State.RUNNING && r == null; i++) {
                    Thread.yield();
                    r = tasks.poll();
                }

                if (curState == State.RUNNING) {
                    if (r != null)
                        r.run();
                    else if (parkThread(Thread.currentThread()))
                        return;
                }
            }
        }

        @Override
        public void afterProcess() {

        }
    }
}

