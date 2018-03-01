package producerConsumer;

public interface IWaitStrategy {

    boolean canRun() throws InterruptedException;

    void trySpinWait() throws InterruptedException;
}
