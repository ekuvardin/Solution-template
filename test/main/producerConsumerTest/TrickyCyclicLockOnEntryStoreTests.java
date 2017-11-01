package main.producerConsumerTest;

import main.producerConsumer.FIFO.CyclicLockOnEntryStore;
import main.producerConsumer.FIFO.TrickyCyclicLockOnEntryStore;

public class TrickyCyclicLockOnEntryStoreTests extends CommonTests {

    @Override
    public void preTest() {
        this.store = new TrickyCyclicLockOnEntryStore<>(100);
    }
}
