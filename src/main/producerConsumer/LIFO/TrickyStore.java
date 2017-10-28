//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package main.producerConsumer.LIFO;

import main.producerConsumer.IStore;

import java.lang.reflect.Array;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.locks.ReentrantLock;

public class TrickyStore<T> implements IStore<T> {
    protected final int maxSize;
    protected final T[] array;
    protected volatile int currentSize;
    protected final ReentrantLock lock = new ReentrantLock();

    protected AtomicIntegerFieldUpdater<TrickyStore> currentSizeUpdater = AtomicIntegerFieldUpdater.newUpdater(TrickyStore.class, "currentSize");

    public TrickyStore(int maxSize, Class<T> cls) {
        this.maxSize = maxSize;
        this.currentSize = 0;
        this.array = ((T[]) Array.newInstance(cls, maxSize));
    }

    public T get() throws InterruptedException {
        {
            do {
                if (this.currentSize > 0) {
                    this.lock.lockInterruptibly();

                    try {
                        if (this.currentSize > 0) {
                            currentSizeUpdater.lazySet(this, this.currentSize - 1);
                            T item = this.array[this.currentSize];
                            this.array[this.currentSize] = null;

                            return item;
                        }
                    } finally {
                        this.lock.unlock();
                    }
                }

                Thread.yield();
            } while (!Thread.interrupted());
        }

        throw new InterruptedException();
    }

    public void put(T item) throws InterruptedException {
        do {
            if (this.currentSize < this.maxSize) {
                this.lock.lockInterruptibly();

                try {
                    if (this.currentSize < this.maxSize) {
                        this.array[this.currentSize] = item;
                        currentSizeUpdater.lazySet(this, this.currentSize + 1);
                        return;
                    }
                } finally {
                    this.lock.unlock();
                }
            }

            Thread.yield();
        } while (!Thread.interrupted());

        throw new InterruptedException();
    }

    public boolean IsEmpty() {
        return this.currentSize == 0;
    }
}
