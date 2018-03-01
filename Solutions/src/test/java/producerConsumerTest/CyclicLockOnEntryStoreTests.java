package producerConsumerTest;


import producerConsumer.FIFO.CyclicLockOnEntryStore;
import producerConsumer.ThreadInterruptedStrategy;

public class CyclicLockOnEntryStoreTests extends CommonTests {

    @Override
    public void preTest() {
        this.store = new CyclicLockOnEntryStore<>(100, new ThreadInterruptedStrategy());
    }
}
