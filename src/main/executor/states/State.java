package main.executor.states;

import main.executor.executionStrategy.EndExecution;
import main.executor.executionStrategy.IExecutionStrategy;
import main.executor.executionStrategy.SoftShutdown;
import main.executor.executionStrategy.SpinStrategy;

/**
 * Executor state
 */
public enum State {

    RUNNING {
        @Override
        public boolean canBeChange(State nextState) {
            return nextState == EXECUTE_ALL_TASK_THEN_TERMINATE || nextState == TERMINATE;
        }

        @Override
        public IExecutionStrategy getStrategy() {
            return new SpinStrategy();
        }

    },
    EXECUTE_ALL_TASK_THEN_TERMINATE {
        @Override
        public boolean canBeChange(State nextState) {
            return nextState == TERMINATE || nextState == this;
        }

        @Override
        public IExecutionStrategy getStrategy() {
            return new SoftShutdown();
        }
    },
    TERMINATE {
        @Override
        public boolean canBeChange(State nextState) {
            return nextState == this;
        }

        @Override
        public IExecutionStrategy getStrategy() {
            return new EndExecution();
        }
    };

    /**
     * Can current state changed to nextState
     *
     * @param nextState state to be changed
     * @return Can current state changed to nextState
     */
    public abstract boolean canBeChange(State nextState);

    /**
     * Create strategy for current state
     *
     * @return new strategy
     */
    public abstract IExecutionStrategy getStrategy();
}
