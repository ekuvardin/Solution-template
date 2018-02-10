package main.producerConsumer.LIFO;

import main.producerConsumer.IStore;
import main.producerConsumer.IWaitStrategy;

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
    protected final IWaitStrategy waitStrategy;

    protected final ReentrantLock lock = new ReentrantLock();

    public Store(int maxSize, Class<T> cls, IWaitStrategy waitStrategy) {
        this.maxSize = maxSize;
        this.currentSize = 0;
        this.array = (T[]) Array.newInstance(cls, this.maxSize);
        this.waitStrategy = waitStrategy;
    }

    @Override
    public T get() throws InterruptedException {
        while (waitStrategy.canRun()) {
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
            waitStrategy.trySpinWait();
        }

        throw new InterruptedException();
    }

    @Override
    public void put(T item) throws InterruptedException {
        while (waitStrategy.canRun()) {
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
            waitStrategy.trySpinWait();
        }
    }

    @Override
    public boolean IsEmpty() {
        return currentSize == 0;
    }

    @Override
    public void clear() throws InterruptedException {
        while (true) {
            //Simple TTAS
            if (currentSize > 0) {
                lock.lockInterruptibly();
                try {
                    for (int i = 0; i < currentSize; i++)
                        array[currentSize--] = null;
                    return;
                } finally {
                    lock.unlock();
                }
            }
            waitStrategy.trySpinWait();
        }
    }
}
