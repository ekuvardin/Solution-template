package main.producerConsumer;

public class ThreadInterruptedStrategy implements IWaitStrategy {

    @Override
    public boolean tryRun() throws InterruptedException {
        return !Thread.currentThread().isInterrupted();
    }

    @Override
    public void trySpinWait() throws InterruptedException {
        Thread.yield();
    }
}
