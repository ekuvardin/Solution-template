package main.producerConsumerTest;

import main.producerConsumer.FIFO.CyclicLockOnEntryStore;
import main.producerConsumer.LIFO.StoreWithPark;

public class CyclicLockOnEntryStoreTests extends CommonTests {

    @Override
    public void preTest() {
        this.store = new CyclicLockOnEntryStore<>(100);
    }
}