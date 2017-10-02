package main.producerConsumer;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class StoreOnArray<T> implements IStore<T> {

    private final AtomicReferenceArray<T> array;
    private final IIndexStrategy indexStrategy;
    private final Random random = new Random();

    public StoreOnArray(int size) {
        array = new AtomicReferenceArray<>(size);

        if ((size & -size) == size) {
            indexStrategy = ((p1) -> (p1 + 1) & array.length());
        } else {
            indexStrategy = ((p1) -> (p1 + 1) % array.length());
        }
    }

    @Override
    public T get() {
        int localIndex = random.nextInt(array.length());

        T item;
        while (array.get(localIndex) == null || (item = array.getAndSet(localIndex, null)) == null) {
            localIndex = indexStrategy.getIndex(localIndex);
        }

        return item;
    }

    @Override
    public void put(T input) {
        int localIndex = random.nextInt(array.length());
        while (!array.compareAndSet(localIndex, null, input)) {
            localIndex = indexStrategy.getIndex(localIndex);
        }
    }

    @FunctionalInterface
    protected interface IIndexStrategy {
        int getIndex(int p1);

    }

    @Override
    public boolean IsEmpty() {
        for (int i = 0; i < array.length(); i++) {
            if (array.get(i) != null) {
                return false;
            }
        }

        return true;
    }
}
