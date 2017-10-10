package main.executor.states;

import javafx.util.Pair;
import main.executor.executionStrategy.IExecutionStrategy;

import javax.annotation.Nonnull;

/**
 * Links Executor state and strategy together
 * Executor shouldn't know how change state and State shouldn't know how to execute strategy
 */
public class StateMachine {

    @Nonnull
    private volatile Pair<State, IExecutionStrategy> currentWork = new Pair<>(State.RUNNING, State.RUNNING.getStrategy());

    /**
     * Try to change current state to newState
     *
     * @param newState state to be changed
     */
    public void changeState(State newState) {
        synchronized (this) {
            if (currentWork.getKey().canBeChange(newState)) {
                Pair<State, IExecutionStrategy> old = currentWork;
                currentWork = new Pair<>(newState, newState.getStrategy());
                old.getValue().stopExecution();
            } else {
                throw new RuntimeException(String.format("Can't set new state:%s after:%s", newState, currentWork.getKey()));
            }
        }
    }

    /**
     * Get current executing strategy
     *
     * @return current strategy
     */
    public IExecutionStrategy getCurrentStrategy() {
        return currentWork.getValue();
    }
}
