package main.producerConsumer;

import java.lang.reflect.Array;

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
                if (getSize() == 0) {
                    entry.wait();
                }

                if (localIndex == indexStrategy.getIndex(head)) {
                    result = entry.value;
                    entry.setValue(null);
                    head++;
                    entry.notifyAll();
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
                if (getSize() == capacity) {
                    entry.wait();
                }

                if (localIndex == indexStrategy.getIndex(tail)) {
                    entry.setValue(input);
                    tail++;
                    entry.notifyAll();
                    return;
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
