package main.producerConsumerTest;

import main.producerConsumer.FIFO.TwoLocksStore;

public class TwoLocksStoreTests extends CommonTests {

    @Override
    public void preTest() {
        this.store = new TwoLocksStore<>(100);
    }
}
