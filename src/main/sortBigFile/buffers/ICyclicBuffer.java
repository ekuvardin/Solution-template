package main.sortBigFile.buffers;

public interface ICyclicBuffer<T extends Comparable<T>> {

    T getFirst();

    T getLast();

    int getSize();

    int getCapacity();

    void put(T value);

    T pull();
}
