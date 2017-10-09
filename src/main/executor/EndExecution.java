package main.executor;

import java.util.Queue;

class EndExecution implements IExecutionStrategy {

    @Override
    public boolean process(Queue<Runnable> tasks, StateMachine stateMachine) {
        return false;
    }
}
