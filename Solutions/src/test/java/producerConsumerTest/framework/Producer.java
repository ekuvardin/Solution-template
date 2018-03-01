package producerConsumerTest.framework;

import producerConsumer.IStore;

public class Producer implements StoreStrategy<Integer> {

    @Override
    public int getResults(IStore<Integer> store, int count) throws InterruptedException {
        int tmp = count;
        do {
            store.put(--tmp);
        } while (tmp > 0);

        return tmp;
    }
}
