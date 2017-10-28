package main.producerConsumerTest;

import main.producerConsumer.LIFO.Store;
import org.junit.Before;

public class StoreTest extends CommonTests {

    @Before
    public void preTest() {
        this.store = new Store<>(50, Integer.class);
    }
}
