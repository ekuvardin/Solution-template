package main.producerConsumer;

public interface IWaitStrategy {

    boolean tryRun() throws InterruptedException;

    void trySpinWait() throws InterruptedException;
}
