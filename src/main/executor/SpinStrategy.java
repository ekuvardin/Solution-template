package main.executor;

import java.util.Queue;

class SpinStrategy implements IExecutionStrategy {

    private final int spinCount = 100;
    private final Object lock = new Object();

    @Override
    public boolean process(Queue<Runnable> tasks, StateMachine stateMachine) {
        boolean res = true;
        do {
            Runnable r = tasks.poll();

            // simple spin
            for (int i = 0; i < spinCount && stateMachine.getCurrentState() == StateMachine.State.RUNNING && r == null; i++) {
                Thread.yield();
                r = tasks.poll();
            }

            // found task
            if (stateMachine.getCurrentState() == StateMachine.State.RUNNING && r != null)
                r.run();

        } while (stateMachine.getCurrentState() == StateMachine.State.RUNNING && !(res = parkThread(tasks, stateMachine)));// park thread when spin too long

        return res;
    }

    private boolean parkThread(Queue<Runnable> tasks, StateMachine stateMachine) {
        synchronized (lock) {
            if (tasks.isEmpty() && stateMachine.getCurrentState() == StateMachine.State.RUNNING) {
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
