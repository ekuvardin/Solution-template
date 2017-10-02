package main.producerConsumerTest.framework;

import main.producerConsumer.IStore;
import main.producerConsumer.StoreOnArray;
import main.producerConsumerTest.StoreOnArrayTests;

public class Consumer extends RunnerTemplate {

    public Consumer(IStore<Integer> store, int count) {
        super(store, count);
    }

    @Override
    public void run() {
        do {
            store.get();
            count--;
        } while (count > 0);
    }
}
