package main.producerConsumerTest;

import main.producerConsumer.FIFO.TwoLocksStore;
import main.producerConsumer.ThreadInterruptedStrategy;

public class TwoLocksStoreTests extends CommonTests {

    @Override
    public void preTest() {
        this.store = new TwoLocksStore<>(100, new ThreadInterruptedStrategy());
    }
}
