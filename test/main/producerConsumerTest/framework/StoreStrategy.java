package main.producerConsumerTest.framework;

import main.producerConsumer.IStore;

@FunctionalInterface
public interface StoreStrategy<T> {

    int getResults(IStore<T> store, int count);

}
