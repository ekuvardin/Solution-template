package main.producerConsumer.LIFO;

import main.producerConsumer.IStore;

import java.lang.reflect.Array;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simple store using TTAS na lock to guard array
 *
 * @param <T> stored item
 */
public class Store<T> implements IStore<T> {
    protected final int maxSize;
    protected final T[] array;
    protected volatile int currentSize;

    protected final ReentrantLock lock = new ReentrantLock();

    public Store(int maxSize, Class<T> cls) {
        this.maxSize = maxSize;
        this.currentSize = 0;
        array = (T[]) Array.newInstance(cls, maxSize);
    }

    @Override
    public T get() throws InterruptedException {
        while (true) {
            //Simple TTAS
            if (currentSize > 0) {
                lock.lockInterruptibly();
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

            // On production environment you can remove this line
            // This is workaround for jmh tests. Removing during running benchmarks tends to hanging tests.
            if (Thread.interrupted())
                throw new InterruptedException();
        }
    }

    @Override
    public void put(T item) throws InterruptedException {
        while (true) {
            //Simple TTAS
            if (currentSize < maxSize) {
                lock.lockInterruptibly();
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

            // On production environment you can remove this line
            // This is workaround for jmh tests. Removing during running benchmarks tends to hanging tests.
            if (Thread.interrupted())
                throw new InterruptedException();
        }
    }

    @Override
    public boolean IsEmpty() {
        return currentSize == 0;
    }
}
