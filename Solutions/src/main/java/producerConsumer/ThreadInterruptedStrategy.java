package producerConsumer;

public class ThreadInterruptedStrategy implements IWaitStrategy {

    @Override
    public boolean canRun() throws InterruptedException {
        return !Thread.currentThread().isInterrupted();
    }

    @Override
    public void trySpinWait() throws InterruptedException {
        Thread.yield();
    }
}
