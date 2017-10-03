package main.producerConsumer;

import java.lang.reflect.Array;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simple store using TTAS na lock to guard array
 *
 * @param <T> stored item
 */
public class Store<T> implements IStore<T> {
    private final int maxSize;
    private final T[] array;
    private volatile int currentSize;

    private final ReentrantLock lock = new ReentrantLock();

    public Store(int maxSize, Class<T> cls) {
        this.maxSize = maxSize;
        this.currentSize = 0;
        array = (T[]) Array.newInstance(cls, maxSize);
    }

    @Override
    public T get() {
        while (true) {
            //Simple TTAS
            if (currentSize > 0 && lock.tryLock()) {
                try {
                    // Do check on more time because currentSize may change
                    if (currentSize > 0) {
                        T item = array[currentSize - 1];
                        array[--currentSize] = null;
                        return item;
                    }
                } finally {
                    lock.unlock();
                }
            }
            Thread.yield();
        }
    }

    @Override
    public void put(T item) {
        while (true) {
            //Simple TTAS
            if (currentSize < maxSize && lock.tryLock()) {
                try {
                    if (currentSize < maxSize) {
                        array[currentSize++] = item;
                        return;
                    }
                } finally {
                    lock.unlock();
                }
            }
            Thread.yield();
        }
    }

    @Override
    public boolean IsEmpty() {
        return currentSize == 0;
    }
}
