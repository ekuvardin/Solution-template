package main.executor;

import javax.annotation.Nonnull;

public class StateMachine {

    @Nonnull
    private volatile State currentState = State.RUNNING;

    public synchronized void changeState(State newState){
        if(currentState.canBeChange(newState)){
            State old = newState;
            if(old == State.RUNNING)


            currentState = newState;
        }
    }

    @Nonnull
    public State getCurrentState() {
        return currentState;
    }

}
