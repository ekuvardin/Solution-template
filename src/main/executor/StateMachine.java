package main.executor;

import javax.annotation.Nonnull;
import java.util.Queue;

public class StateMachine {

    @Nonnull
    private volatile State currentState = State.RUNNING;

    private synchronized void changeState(State newState){
        if(currentState.canBeChange(newState)){
            currentState = newState;
        }
    }

    @Nonnull
    public State getCurrentState() {
        return currentState;
    }

    public enum State {
        RUNNING {
            @Override
            public boolean canBeChange(State nextState) {
                return nextState == EXECUTE_ALL_TASK_THEN_TERMINATE || nextState == TERMINATE;
            }

        },
        EXECUTE_ALL_TASK_THEN_TERMINATE {
            @Override
            public boolean canBeChange(State nextState) {
                return nextState == TERMINATE;
            }
        },
        TERMINATE {
            @Override
            public boolean canBeChange(State nextState) {
                return false;
            }
        };

        public abstract boolean canBeChange(State nextState);
    }

}
