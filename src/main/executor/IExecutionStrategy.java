package main.executor;

import java.util.Queue;

@FunctionalInterface
interface IExecutionStrategy {

    boolean process(Queue<Runnable> tasks, StateMachine stateMachine);

}
