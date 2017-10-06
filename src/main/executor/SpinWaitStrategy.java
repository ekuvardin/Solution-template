package main.executor;

public class SpinWaitStrategy implements IWaitStrategy {

    private final Object lock = new Object();

    @Override
    public void wait(int milSeconds) {
        synchronized ( lock ) {
           /* if ( maxWaitMs > 0 ) {
                lock.wait( maxWaitMs );
            } else {
                lock.wait();
            }*/
        }
    }

    @Override
    public void notifyWaiter() {

    }
}
