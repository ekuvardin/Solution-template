package main.producerConsumerTest.framework;

import main.producerConsumer.IStore;
import main.producerConsumer.StoreOnArray;
import main.producerConsumerTest.StoreOnArrayTests;

public class Producer extends RunnerTemplate {

    public Producer(IStore<Integer> store, int count) {
        super(store, count);
    }

    @Override
    public void run() {
        do {
            store.put(--count);
        } while (count > 0);
    }
}
