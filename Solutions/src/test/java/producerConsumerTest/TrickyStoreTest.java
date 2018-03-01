package producerConsumerTest;

import org.junit.Before;
import producerConsumer.LIFO.TrickyStore;
import producerConsumer.ThreadInterruptedStrategy;

public class TrickyStoreTest extends CommonTests {

    @Before
    public void preTest() {
        this.store = new TrickyStore<>(50, Integer.class, new ThreadInterruptedStrategy());
    }
}
