package producerConsumerTest;


import producerConsumer.FIRO.RandomStartStore;
import producerConsumer.ThreadInterruptedStrategy;

public class RandomStartStoreTests extends CommonTests {

    @Override
    public void preTest() {
        this.store = new RandomStartStore<>(50, new ThreadInterruptedStrategy() );
    }
}
