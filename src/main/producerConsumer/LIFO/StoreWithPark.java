package main.producerConsumer.LIFO;

import java.util.concurrent.locks.Condition;

public class StoreWithPark<T> extends Store<T> {

    protected final int spinCount;
    protected final Condition notEmpty = lock.newCondition();
    protected final Condition notFull = lock.newCondition();

    public StoreWithPark(int maxSize, Class cls, int spinCount) {
        super(maxSize, cls);
        this.spinCount = spinCount;
    }

    @Override
    public T get() throws InterruptedException {
        for (int i = 0; i < spinCount; i++) {
            if (currentSize > 0) {
                //Simple TTAS
                lock.lockInterruptibly();
                try {
                    // Do check on more time because currentSize may change
                    if (currentSize > 0) {
                        return getItem();
                    }
                } finally {
                    lock.unlock();
                }
            }
            Thread.yield();
        }

        return getItemWithPark();

    }

    @Override
    public void put(T item) throws InterruptedException {
        for (int i = 0; i < spinCount; i++) {
            //Simple TTAS
            if (currentSize < maxSize) {
                lock.lockInterruptibly();
                try {
                    if (currentSize < maxSize) {
                        putItem(item);
                        return;
                    }
                } finally {
                    lock.unlock();
                }
            }
            Thread.yield();
        }

        putItemWithPark(item);
    }

    private T getItemWithPark() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (currentSize == 0)
                notEmpty.await();

            return getItem();
        } finally {
            lock.unlock();
        }
    }

    private void putItemWithPark(T item) throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (currentSize == maxSize)
                notFull.await();

            putItem(item);
        } finally {
            lock.unlock();
        }
    }

    private T getItem() {
        T item = array[currentSize - 1];
        array[--currentSize] = null;
        notFull.signal();
        return item;
    }

    private void putItem(T item) {
        array[currentSize++] = item;
        notEmpty.signal();
    }

}
