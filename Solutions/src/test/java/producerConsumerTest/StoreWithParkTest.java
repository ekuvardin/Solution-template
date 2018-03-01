package producerConsumerTest;

import org.junit.Before;
import producerConsumer.LIFO.StoreWithPark;
import producerConsumer.ThreadInterruptedStrategy;

public class StoreWithParkTest extends CommonTests {

    @Before
    public void preTest() {
        this.store = new StoreWithPark<>(50, Integer.class, 100, new ThreadInterruptedStrategy());
    }
}
