package main.producerConsumerTest.framework;

import main.producerConsumer.IStore;

public class Producer implements StoreStrategy<Integer> {

    @Override
    public int getResults(IStore<Integer> store, int count) {
        int tmp = count;
        do {
            store.put(--tmp);
        } while (tmp > 0);

        return tmp;
    }
}
