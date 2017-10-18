package main.producerConsumerTest;

import main.producerConsumer.CyclicLockOnEntryStore;
import main.producerConsumer.StoreWithPark;

public class RandomStartStoreTests extends CommonTests {

    @Override
    public void preTest() {
        this.store = new StoreWithPark<>(50, Integer.class, 100);
    }
}
