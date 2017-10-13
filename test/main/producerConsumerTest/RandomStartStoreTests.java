package main.producerConsumerTest;

import main.producerConsumer.RandomStartStore;

public class RandomStartStoreTests extends CommonTests {

    @Override
    public void preTest() {
        this.store = new RandomStartStore<>(50);
    }
}
