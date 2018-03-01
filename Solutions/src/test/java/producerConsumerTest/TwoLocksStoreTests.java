package producerConsumerTest;


import producerConsumer.FIFO.TwoLocksStore;
import producerConsumer.ThreadInterruptedStrategy;

public class TwoLocksStoreTests extends CommonTests {

    @Override
    public void preTest() {
        this.store = new TwoLocksStore<>(100, new ThreadInterruptedStrategy());
    }
}
