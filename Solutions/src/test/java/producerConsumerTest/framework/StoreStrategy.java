package producerConsumerTest.framework;


import producerConsumer.IStore;

@FunctionalInterface
public interface StoreStrategy<T> {

    int getResults(IStore<T> store, int count) throws InterruptedException;

}
