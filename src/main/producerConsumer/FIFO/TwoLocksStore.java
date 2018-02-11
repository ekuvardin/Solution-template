package main.producerConsumer.FIFO;

import main.producerConsumer.IIndexStrategy;
import main.producerConsumer.IStore;
import main.producerConsumer.IWaitStrategy;

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
    private final IWaitStrategy waitStrategy;

    private final ReentrantLock headLock = new ReentrantLock();
    private final ReentrantLock tailLock = new ReentrantLock();

    private int head = 0;

    private int tail = 0;

    public TwoLocksStore(int size, IWaitStrategy waitStrategy) {
        this.array = new AtomicReferenceArray<>(size);

        if ((size & -size) == size) {
            this.indexStrategy = ((p1) -> p1 & (size - 1));
        } else {
            this.indexStrategy = ((p1) -> p1 % size);
        }
        this.waitStrategy = waitStrategy;
    }

    @Override
    public T get() throws InterruptedException {
        for (T result = null; waitStrategy.canRun(); waitStrategy.trySpinWait()) {
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
        return null;
    }

    public synchronized int getSize() {
        return tail - head;
    }

    @Override
    public void put(T input) throws InterruptedException {
        while (waitStrategy.canRun()) {
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
            waitStrategy.trySpinWait();
        }
    }

    @Override
    public boolean IsEmpty() {
        return getSize() == 0;
    }

    @Override
    public void clear() throws InterruptedException {
        for (; waitStrategy.canRun(); waitStrategy.trySpinWait()) {
            if (headLock.tryLock()) {
                try {
                    int localIndex = indexStrategy.getIndex(head);
                    while (array.get(localIndex) != null) {
                        array.set(localIndex, null);
                        head++;
                        localIndex = indexStrategy.getIndex(head);
                    }
                    return;
                } finally {
                    headLock.unlock();
                }
            }
        }
    }
}
