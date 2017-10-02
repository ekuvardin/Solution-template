package main.producerConsumer;

public interface IStore<T> {

    T get();

    void put(T item);

    boolean IsEmpty();
}
