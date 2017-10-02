package main.producerConsumerTest;

import main.producerConsumer.StoreOnArray;

public class StoreOnArrayTests extends CommonTests {

    @Override
    public void preTest() {
        this.store = new StoreOnArray<>(50);
    }
}
