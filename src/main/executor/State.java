package main.executor;

import java.util.Queue;

public enum State implements IExecutionStrategy {

    RUNNING {
        private final int spinCount = 100;
        private final Object lock = new Object();

        @Override
        public boolean canBeChange(State nextState) {
            return nextState == EXECUTE_ALL_TASK_THEN_TERMINATE || nextState == TERMINATE;
        }

        @Override
        public boolean process(Queue<Runnable> tasks, StateMachine stateMachine) {
            boolean res = true;
            do {
                Runnable r = tasks.poll();

                // simple spin
                for (int i = 0; i < spinCount && stateMachine.getCurrentState() == State.RUNNING && r == null; i++) {
                    Thread.yield();
                    r = tasks.poll();
                }

                // found task
                if (stateMachine.getCurrentState() == State.RUNNING && r != null)
                    r.run();

            }
            while (stateMachine.getCurrentState() == State.RUNNING && !(res = parkThread(tasks, stateMachine)));// park thread when spin too long

            return res;
        }

        private boolean parkThread(Queue<Runnable> tasks, StateMachine stateMachine) {
            synchronized (lock) {
                if (tasks.isEmpty() && stateMachine.getCurrentState() == State.RUNNING) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        return false;
                    }
                }
            }

            return true;
        }

        public void unParkThread() {
            synchronized (lock) {
                lock.notify();
            }
        }

        public void unParkAllThread() {
            synchronized (lock) {
                lock.notifyAll();
            }
        }

    },
    EXECUTE_ALL_TASK_THEN_TERMINATE {
        @Override
        public boolean canBeChange(State nextState) {
            return nextState == TERMINATE;
        }

        @Override
        public boolean process(Queue<Runnable> tasks, StateMachine stateMachine) {
            for (Runnable r = tasks.poll(); stateMachine.getCurrentState() == State.EXECUTE_ALL_TASK_THEN_TERMINATE && r != null; r = tasks.poll()) {
                r.run();
                Thread.yield();
            }

            return false;
        }
    },
    TERMINATE {
        @Override
        public boolean canBeChange(State nextState) {
            return false;
        }

        @Override
        public boolean process(Queue<Runnable> tasks, StateMachine stateMachine) {
            return false;
        }
    };

    public abstract boolean canBeChange(State nextState);

}
