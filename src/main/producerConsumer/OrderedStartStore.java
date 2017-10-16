package main.producerConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.AtomicStampedReference;

public class OrderedStartStore<T> implements IStore<T> {

    private final List<AtomicStampedReference<T>> array;
    private final RandomStartStore.IIndexStrategy indexStrategy;
    private volatile int first = -1;

    public OrderedStartStore(int size) {
        array = new ArrayList<>(size);

        if ((size & -size) == size) {
            indexStrategy = ((p1) -> (p1 + 1) & (size - 1));
        } else {
            indexStrategy = ((p1) -> (p1 + 1) % size);
        }
    }

    @Override
    public T get() throws InterruptedException {
        int localIndex = indexStrategy.getIndex(first);

        T item = null;
        AtomicStampedReference ref = array.get(localIndex);
        int stamp = ref.getStamp();
        while (ref.getReference() == null || (item = ref.compareAndSet(ref.getReference(),null,stamp,stamp+1 )) == null) && !Thread.interrupted()) {
            localIndex = indexStrategy.getIndex(localIndex);

            // Thread.yield();
        }

        if (Thread.interrupted()) {
            throw new InterruptedException();
        }

        lastUsed.set(localIndex);
        return item;
    }

    @Override
    public void put(T input) throws InterruptedException {
        boolean exit = false;
        for (int localIndex = indexStrategy.getIndex(first); !exit && !Thread.interrupted(); ) {
            while (localIndex != first && !(exit = array.get(localIndex).compareAndSet(null, input, 0, first)) && !Thread.interrupted()) {
                localIndex = indexStrategy.getIndex(localIndex);
            }
        }

        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }

    @FunctionalInterface
    protected interface IIndexStrategy {
        int getIndex(int p1);
    }

    @Override
    public boolean IsEmpty() {
        // Yes, I know there is no thread safe when some threads are continue executing
        // This methods need me for test(Also know it's bad practise)
        for (int i = 0; i < array.length(); i++) {
            if (array.get(i) != null) {
                return false;
            }
        }

        return true;
    }
}
