package main.producerConsumerTest;

import main.producerConsumer.LIFO.StoreWithPark;
import main.producerConsumer.LIFO.TrickyStore;
import org.junit.Before;

public class StoreWithParkTest extends CommonTests {

    @Before
    public void preTest() {
        this.store = new StoreWithPark<>(50, Integer.class, 100);
    }
}
