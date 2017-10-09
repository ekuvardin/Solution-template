package main.executor;

import java.util.Queue;

class SoftShutdown implements IExecutionStrategy {

    @Override
    public boolean process(Queue<Runnable> tasks, StateMachine stateMachine) {
        for (Runnable r = tasks.poll(); stateMachine.getCurrentState() == StateMachine.State.EXECUTE_ALL_TASK_THEN_TERMINATE && r != null; r = tasks.poll()) {
            r.run();
            Thread.yield();
        }

        return false;
    }
}
