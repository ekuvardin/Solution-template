package main.producerConsumer;

public class ThreadInterruptedStrategy implements IWaitStrategy {
    @Override
    public boolean continueWork(int idleCounter) throws InterruptedException {
        if(Thread.currentThread().isInterrupted())
            return false;
        return true;
    }
}
