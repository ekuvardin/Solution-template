package executor.states;

import executor.executionStrategy.IExecutionStrategy;

import javax.annotation.Nonnull;

/**
 * Links Executor state and strategy together
 * Executor shouldn't know how change state and State shouldn't know how to execute strategy
 */
public class StateMachine {

    @Nonnull
    private volatile Link currentWork = new Link(State.RUNNING);

    /**
     * Try to change current state to newState
     *
     * @param newState state to be changed
     */
    public void changeState(State newState) {
        synchronized (this) {
            if (currentWork.getState().canBeChange(newState)) {
                Link old = currentWork;
                currentWork = new Link(newState);
                old.getExecutionStrategy().stopExecution();
            } else {
                throw new RuntimeException(String.format("Can't set new state:%s after:%s", newState, currentWork.getState()));
            }
        }
    }

    /**
     * Get current executing strategy
     *
     * @return current strategy
     */
    public IExecutionStrategy getCurrentStrategy() {
        return currentWork.getExecutionStrategy();
    }

    private static class Link {
        final State state;
        final IExecutionStrategy executionStrategy;

        Link(State state) {
            this.state = state;
            this.executionStrategy = state.getStrategy();
        }

        State getState() {
            return state;
        }

        IExecutionStrategy getExecutionStrategy() {
            return executionStrategy;
        }
    }
}
