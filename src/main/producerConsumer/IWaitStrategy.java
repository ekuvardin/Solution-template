package main.producerConsumer;

public interface IWaitStrategy {

    boolean continueWork(int idleCounter) throws InterruptedException;

}
