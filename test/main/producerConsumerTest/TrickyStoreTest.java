package main.producerConsumerTest;

import main.producerConsumer.LIFO.Store;
import main.producerConsumer.LIFO.TrickyStore;
import main.producerConsumer.ThreadInterruptedStrategy;
import org.junit.Before;

public class TrickyStoreTest extends CommonTests {

    @Before
    public void preTest() {
        this.store = new TrickyStore<>(50, Integer.class, new ThreadInterruptedStrategy());
    }
}
