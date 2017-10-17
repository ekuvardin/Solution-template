package main.producerConsumerTest;

import main.producerConsumer.CyclicLockOnEntryStore;

public class RandomStartStoreTests extends CommonTests {

    @Override
    public void preTest() {
        this.store = new CyclicLockOnEntryStore<>(50);
    }
}
