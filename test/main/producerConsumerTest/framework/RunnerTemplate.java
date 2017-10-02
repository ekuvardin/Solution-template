package main.producerConsumerTest.framework;

import main.producerConsumer.IStore;

public abstract class RunnerTemplate implements Runnable {
    protected IStore<Integer> store;
    protected volatile int count;

    RunnerTemplate(IStore<Integer> store, int count) {
        this.store = store;
        this.count = count;
    }

    public int getCount() {
        return count;
    }

}
