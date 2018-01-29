package main.producerConsumer.FIFO;

import main.producerConsumer.IStore;

import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Two locks used on head and tail and this variables are not volatile because whe change this values under the same locks.
 *
 * @param <T> type of stored items
 */
public class TwoLocksStore<T> implements IStore<T> {

    private final AtomicReferenceArray<T> array;
    private final IIndexStrategy indexStrategy;

    private final ReentrantLock headLock = new ReentrantLock();
    private final ReentrantLock tailLock = new ReentrantLock();

    private int head = 0;

    private int tail = 0;

    public TwoLocksStore(int size) {
        array = new AtomicReferenceArray<>(size);

        if ((size & -size) == size) {
            indexStrategy = ((p1) -> p1 & (size - 1));
        } else {
            indexStrategy = ((p1) -> p1 % size);
        }
    }

    @Override
    public T get() throws InterruptedException {
        T result = null;
        for(;!Thread.interrupted();  Thread.yield()) {
            if (headLock.tryLock()) {
                try {
                    int localIndex = indexStrategy.getIndex(head);
                    result = array.get(localIndex);
                    if (result != null) {
                        array.set(localIndex, null);
                        head++;
                        return result;
                    }
                } finally {
                    headLock.unlock();
                }
            }
        }

        throw new InterruptedException();
    }

    public synchronized int getSize() {
        return tail - head;
    }

    @Override
    public void put(T input) throws InterruptedException {
        while (!Thread.interrupted()) {
            if (tailLock.tryLock()) {
                try {
                    int localIndex = indexStrategy.getIndex(tail);
                    if (array.compareAndSet(localIndex, null, input)) {
                        tail++;
                        return;
                    }
                } finally {
                    tailLock.unlock();
                }
            }
            Thread.yield();
        }

        throw new InterruptedException();
    }

    @FunctionalInterface
    protected interface IIndexStrategy {
        int getIndex(int p1);
    }

    @Override
    public boolean IsEmpty() {
        return getSize() == 0;
    }
}
