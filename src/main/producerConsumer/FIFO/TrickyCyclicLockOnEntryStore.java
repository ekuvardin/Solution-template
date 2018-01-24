package main.producerConsumer.FIFO;

import main.producerConsumer.IStore;
import sun.jvm.hotspot.runtime.Threads;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Array;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

/**
 * Tricky implementation because we use CyclicLockOnEntryStore + lazySet
 *
 * lazySet means that  The semantics are that the write is guaranteed not to be re-ordered with any previous write,
 * but may be reordered with subsequent operations
 * lazySet provides a store-store barrier before write means(all writes must be commited before lazyset)
 * but no store-load barrier after lazyset as ordinary volatile write does
 *
 * On x86 processors store-load barrier is
 * lock add %esp, 0x00
 * which is quite expensive
 *
 * For more info see
 * See http://dev.cheremin.info/2011/10/atomicxxxlazyset.html
 *
 *
 * @param <T> type of stored items
 */
public class TrickyCyclicLockOnEntryStore<T> implements IStore<T> {

    private final Entry[] array;
    private final IIndexStrategy indexStrategy;

  /*  private final LongAdder head = new LongAdder();

    private final LongAdder tail = new LongAdder();*/
   private volatile int head = 0;

    private volatile int tail = 0;

    private final int capacity;

    private final AtomicLongFieldUpdater<TrickyCyclicLockOnEntryStore> headUpdater = AtomicLongFieldUpdater.newUpdater(TrickyCyclicLockOnEntryStore.class, "head");
    private final AtomicLongFieldUpdater<TrickyCyclicLockOnEntryStore> tailUpdater = AtomicLongFieldUpdater.newUpdater(TrickyCyclicLockOnEntryStore.class, "tail");

 /*   private static final VarHandle headUpdater;
    private static final VarHandle tailUpdater;
    static {
        try {
            MethodHandles.Lookup lookUp = MethodHandles.lookup();
            headUpdater = lookUp.findVarHandle(TrickyCyclicLockOnEntryStore.class, "head", long.class);
            tailUpdater = lookUp.findVarHandle(TrickyCyclicLockOnEntryStore.class, "tail", long.class);
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }*/

    public TrickyCyclicLockOnEntryStore(int size) {
        array = (TrickyCyclicLockOnEntryStore.Entry[]) Array.newInstance(TrickyCyclicLockOnEntryStore.Entry.class, size);

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
                  //  head++;
                  //  headUpdater.setRelease(this, head + 1);
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
        return tail - head;
    }

    @Override
    public void put(T input) throws InterruptedException {
        for (int localIndex = indexStrategy.getIndex(tail); ; localIndex = indexStrategy.getIndex(tail)) {
            Entry entry = array[localIndex];
            synchronized (entry) {
                if (localIndex == indexStrategy.getIndex(tail) && getSize() < capacity) {
                    entry.setValue(input);
                    //tail++;
                   // tailUpdater.setRelease(this, tail + 1);
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
        int getIndex(int p1);
    }

    @Override
    public boolean IsEmpty() {
        return getSize() == 0;
    }
}
