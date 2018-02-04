package main.producerConsumer.FIRO;

import main.producerConsumer.IStore;
import main.producerConsumer.IWaitStrategy;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Array based on AtomicReferenceArray when several threads get's there's own
 * random index and scan consistently.
 *
 * @param <T> store elements
 */
public class RandomStartStore<T> implements IStore<T> {

    private final AtomicReferenceArray<T> array;
    private final IIndexStrategy indexStrategy;
    private final Random random = new Random();
    private final IWaitStrategy waitStrategy;

    private final ThreadLocal<Integer> lastPut;
    private final ThreadLocal<Integer> lastGet;
    private final int capacity;

    public RandomStartStore(int size, IWaitStrategy waitStrategy) {
        this.array = new AtomicReferenceArray<>(size);

        if ((size & -size) == size) {
            this.indexStrategy = ((p1) -> (p1 + 1) & (this.array.length() - 1));
        } else {
            this.indexStrategy = ((p1) -> (p1 + 1) % this.array.length());
        }

        this.capacity = size;

        this.lastGet = ThreadLocal.withInitial(() -> this.random.nextInt(this.capacity));
        this.lastPut = ThreadLocal.withInitial(() -> this.random.nextInt(this.capacity));
        this.waitStrategy = waitStrategy;
    }

    @Override
    public T get() throws InterruptedException {
        int localIndex = lastGet.get();

        T item = null;
        while (waitStrategy.tryRun() && (array.get(localIndex) == null || (item = array.getAndSet(localIndex, null)) == null)) {
            localIndex = indexStrategy.getIndex(localIndex);
            if (lastGet.get().equals(localIndex)) {
                waitStrategy.trySpinWait();
            }
        }

        lastGet.set(localIndex);
        return item;
    }

    @Override
    public void put(T input) throws InterruptedException {
        int localIndex = lastPut.get();

        while (waitStrategy.tryRun() && !array.compareAndSet(localIndex, null, input)) {
            localIndex = indexStrategy.getIndex(localIndex);
            if (lastPut.get().equals(localIndex)) {
                waitStrategy.trySpinWait();
            }
        }

        lastPut.set(localIndex);
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
