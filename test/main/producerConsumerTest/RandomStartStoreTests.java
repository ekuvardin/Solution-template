package main.producerConsumerTest;

import main.producerConsumer.FIRO.RandomStartStore;
import main.producerConsumer.ThreadInterruptedStrategy;

public class RandomStartStoreTests extends CommonTests {

    @Override
    public void preTest() {
        this.store = new RandomStartStore<>(50, new ThreadInterruptedStrategy() );
    }
}
