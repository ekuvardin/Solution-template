package main.producerConsumer;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.AtomicStampedReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CyclicStore<T> implements IStore<T> {

    private final AtomicStampedReference<T>[] array;
    private final RandomStartStore.IIndexStrategy indexStrategy;

    private volatile int head;
    private volatile int tail;
    private final int capacity;

    private Lock mainLock = new ReentrantLock();
    private Condition notEmpty = mainLock.newCondition();
    private Condition notFull = mainLock.newCondition();

    public CyclicStore(int size) {
        array = (AtomicStampedReference<T>[]) Array.newInstance(AtomicStampedReference.class, size);
        for (int i = 0; i < size; i++)
            array[i] = new AtomicStampedReference<T>(null, 0);

        if ((size & -size) == size) {
            indexStrategy = ((p1) -> (p1 + 1) & (size - 1));
        } else {
            indexStrategy = ((p1) -> (p1 + 1) % size);
        }

        capacity = size;
    }

    @Override
    public T get() throws InterruptedException {
        T res = null;
        for (int localIndex = head; !array.compareAndSet(localIndex, null, null); localIndex = indexStrategy.getIndex(tail)) {
            if (getSize() > 0) {
                mainLock.lockInterruptibly();
                try {
                    while (getSize() > 0)
                        notEmpty.await();
                } finally {
                    mainLock.unlock();
                }
            }
        }

        mainLock.lockInterruptibly();
        try {
            head++;
            notFull.signal();
        } finally {
            mainLock.unlock();
        }

        return res;
    }

    public int getSize() {
        return tail - head;
    }

    @Override
    public void put(T input) throws InterruptedException {
        int localIndex = indexStrategy.getIndex(tail);
        for(;;){
            AtomicStampedReference<T> value = array[localIndex];
            for (localIndex = indexStrategy.getIndex(tail); !array.compareAndSet(localIndex, null, input); localIndex = indexStrategy.getIndex(tail)) {
                if (getSize() == capacity) {
                    mainLock.lockInterruptibly();
                    try {
                        while (getSize() == capacity)
                            notFull.await();
                    } finally {
                        mainLock.unlock();
                    }
                }
            }
        }


        mainLock.lockInterruptibly();
        try {
            tail++;
            notEmpty.signal();
        } finally {
            mainLock.unlock();
        }
    }

    @FunctionalInterface
    protected interface IIndexStrategy {
        int getIndex(int p1);
    }

    @Override
    public boolean IsEmpty() {
        // Yes, I know there is no thread safe when some threads are continue executing
        // This methods need me for test(Also know it's bad practise)
        for (int i = 0; i < array.length(); i++) {
            if (array.get(i) != null) {
                return false;
            }
        }

        return true;
    }
}
