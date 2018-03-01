package producerConsumerTest;

import org.junit.Before;
import producerConsumer.LIFO.Store;
import producerConsumer.ThreadInterruptedStrategy;

public class StoreTest extends CommonTests {

    @Before
    public void preTest() {
        this.store = new Store<>(50, Integer.class, new ThreadInterruptedStrategy());
    }
}
