package main.producerConsumerTest;

import main.producerConsumer.LIFO.StoreWithPark;

public class RandomStartStoreTests extends CommonTests {

    @Override
    public void preTest() {
        this.store = new StoreWithPark<>(50, Integer.class, 100);
    }
}
