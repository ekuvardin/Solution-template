package producerConsumer.FIFO;

import producerConsumer.IIndexStrategy;
import producerConsumer.IStore;
import producerConsumer.IWaitStrategy;

import java.lang.reflect.Array;
import java.util.concurrent.locks.LockSupport;

/**
 * FIFO queue based on Ring(Cyclic) buffer
 *
 * @param <T> type of stored items
 */
public class CyclicLockOnEntryStore<T> implements IStore<T> {

    private final Entry<T>[] array;
    private final IIndexStrategy indexStrategy;
    private final IWaitStrategy waitStrategy;

    private volatile int head = 0;

    private volatile int tail = 0;

    private final int capacity;

    public CyclicLockOnEntryStore(int size, IWaitStrategy waitStrategy) {
        array = (Entry[]) Array.newInstance(Entry.class, size);
        for (int i = 0; i < size; i++)
            array[i] = new Entry<>();

        if ((size & -size) == size) {
            indexStrategy = ((p1) -> p1 & (size - 1));
        } else {
            indexStrategy = ((p1) -> p1 % size);
        }

        capacity = size;
        this.waitStrategy = waitStrategy;
    }

    @Override
    public T get() throws InterruptedException {
        T result = null;

        for (int localIndex = indexStrategy.getIndex(head); waitStrategy.canRun() && result == null; localIndex = indexStrategy.getIndex(head)) {
            Entry<T> entry = array[localIndex];
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
        for (int localIndex = indexStrategy.getIndex(tail); waitStrategy.canRun(); localIndex = indexStrategy.getIndex(tail)) {
            Entry<T> entry = array[localIndex];
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

    @Override
    public boolean IsEmpty() {
        return getSize() == 0;
    }

    @Override
    public void clear() throws InterruptedException {
        for (int localIndex = indexStrategy.getIndex(head); waitStrategy.canRun() && getSize() > 0; localIndex = indexStrategy.getIndex(head)) {
            Entry<T> entry = array[localIndex];
            synchronized (entry) {
                if (localIndex == indexStrategy.getIndex(head) && getSize() > 0) {
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
    }
}
