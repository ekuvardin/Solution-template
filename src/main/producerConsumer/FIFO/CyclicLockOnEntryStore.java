package main.producerConsumer.FIFO;

import main.producerConsumer.IStore;

import java.lang.reflect.Array;

/**
 * FIFO queue based on Ring(Cyclic) buffer
 *
 * @param <T> type of stored items
 */
public class CyclicLockOnEntryStore<T> implements IStore<T> {

    private final Entry[] array;
    private final IIndexStrategy indexStrategy;

    private volatile int head = 0;

    private volatile int tail = 0;

    private final int capacity;

    public CyclicLockOnEntryStore(int size) {
        array = (Entry[]) Array.newInstance(Entry.class, size);
        for (int i = 0; i < size; i++)
            array[i] = new Entry();

        if ((size & -size) == size) {
            indexStrategy = ((p1) -> p1 & (size - 1));
        } else {
            indexStrategy = ((p1) -> p1 % size);
        }

        capacity = size;
    }

    class Entry {

        private T value = null;

        public void setValue(T value) {
            this.value = value;
        }
    }

    @Override
    public T get() throws InterruptedException {
        T result = null;

        for (int localIndex = indexStrategy.getIndex(head); result == null; localIndex = indexStrategy.getIndex(head)) {
            Entry entry = array[localIndex];
            synchronized (entry) {
                if (localIndex == indexStrategy.getIndex(head) && getSize() > 0) {
                    result = entry.value;
                    entry.setValue(null);
                    head++;
                    entry.notifyAll();
                    // Note: If head==tail => getSize==0 means that queue is empty
                    // and we blocks on node with head==tail
                    // As we use cyclic(ring) buffer then in our situation indexStrategy(head) = indexStrategy(tail)
                    // and we block on the same node which is pointed by tail then next put will wake up us
                } else if (getSize() == 0) {
                    // I don't use loop(as we know that thread may spurious wakeup)
                    // because as I think under contention it's unnecessary.(May be wrong need benchmarks)
                    entry.wait();
                }
            }
        }

        return result;
    }

    public int getSize() {
        return tail - head;
    }

    @Override
    public void put(T input) throws InterruptedException {
        for (int localIndex = indexStrategy.getIndex(tail); ; localIndex = indexStrategy.getIndex(tail)) {
            Entry entry = array[localIndex];
            synchronized (entry) {
                if (localIndex == indexStrategy.getIndex(tail) && getSize() < capacity) {
                    entry.setValue(input);
                    tail++;
                    entry.notifyAll();
                    return;
                    // Note: If head==(tail - capacity) means that queue is full
                    // and we block on node with head==tail - capacity
                    // As we use cyclic(ring) buffer then in our situation indexStrategy(head) = indexStrategy(tail)
                    // and we block on the same node which is pointed by head then next get will wake up us
                } else if (getSize() == capacity) {
                    // I don't use loop(as we know that thread may spurious wakeup)
                    // because as I think under contention it's unnecessary.(May be wrong need benchmarks)
                    entry.wait();
                }
            }
        }
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
