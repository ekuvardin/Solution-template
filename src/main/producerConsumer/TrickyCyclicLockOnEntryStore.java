package main.producerConsumer;

import java.lang.reflect.Array;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

public class TrickyCyclicLockOnEntryStore<T> implements IStore<T> {

    private final Entry[] array;
    private final IIndexStrategy indexStrategy;

    private volatile long head = 0;

    private volatile long tail = 0;

    private final int capacity;

    private final AtomicLongFieldUpdater<TrickyCyclicLockOnEntryStore> headUpdater = AtomicLongFieldUpdater.newUpdater(TrickyCyclicLockOnEntryStore.class, "head");
    private final AtomicLongFieldUpdater<TrickyCyclicLockOnEntryStore> tailUpdater = AtomicLongFieldUpdater.newUpdater(TrickyCyclicLockOnEntryStore.class, "tail");

    public TrickyCyclicLockOnEntryStore(int size, Class<T> cls) {
        array = (TrickyCyclicLockOnEntryStore.Entry[]) Array.newInstance(TrickyCyclicLockOnEntryStore.Entry.class, size);

        for (int i = 0; i < size; i++)
            array[i] = new Entry();

        if ((size & -size) == size) {
            indexStrategy = ((p1) -> (int) p1 & (size - 1));
        } else {
            indexStrategy = ((p1) -> (int) p1 % size);
        }

        capacity = size;
    }

    class Entry {
        T value = null;

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
                    headUpdater.lazySet(this, head + 1);
                    entry.notifyAll();
                } else if (getSize() == 0) {
                    entry.wait();
                }
            }
        }

        return result;
    }

    public int getSize() {
        return (int) (tail - head);
    }

    @Override
    public void put(T input) throws InterruptedException {
        for (int localIndex = indexStrategy.getIndex(tail); ; localIndex = indexStrategy.getIndex(tail)) {
            Entry entry = array[localIndex];
            synchronized (entry) {
                if (localIndex == indexStrategy.getIndex(tail) && getSize() < capacity) {
                    entry.setValue(input);
                    tailUpdater.lazySet(this, tail + 1);
                    entry.notifyAll();
                    return;
                } else if (getSize() == capacity) {
                    entry.wait();
                }
            }
        }
    }

    @FunctionalInterface
    protected interface IIndexStrategy {
        int getIndex(long p1);
    }

    @Override
    public boolean IsEmpty() {
        return getSize() == 0;
    }
}
