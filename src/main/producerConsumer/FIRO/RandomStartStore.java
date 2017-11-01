package main.producerConsumer.FIRO;

import main.producerConsumer.IStore;

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

    private ThreadLocal<Integer> lastUsed;

    public RandomStartStore(int size) {
        array = new AtomicReferenceArray<>(size);

        if ((size & -size) == size) {
            indexStrategy = ((p1) -> (p1 + 1) & (array.length() - 1));
        } else {
            indexStrategy = ((p1) -> (p1 + 1) % array.length());
        }

        lastUsed = ThreadLocal.withInitial(() -> random.nextInt(array.length()));
    }

    @Override
    public T get() throws InterruptedException {
        int localIndex = lastUsed.get();

        T item = null;
        // On production environment you can remove !Thread.currentThread().isInterrupted()
        // This is workaround for jmh tests. Removing during running benchmarks tends to hanging tests.
        while (!Thread.currentThread().isInterrupted() && (array.get(localIndex) == null || (item = array.getAndSet(localIndex, null)) == null)) {
            localIndex = indexStrategy.getIndex(localIndex);
            if(lastUsed.get().equals(localIndex)){
                Thread.yield();
            }
        }

        // On production environment you can remove this line
        // This is workaround for jmh tests. Removing during running benchmarks tends to hanging tests.
        if (Thread.interrupted())
            throw new InterruptedException();

        lastUsed.set(localIndex);
        return item;
    }

    @Override
    public void put(T input) throws InterruptedException {
        int localIndex = lastUsed.get();
        while (!Thread.interrupted() && !array.compareAndSet(localIndex, null, input)) {
            localIndex = indexStrategy.getIndex(localIndex);
            if(lastUsed.get().equals(localIndex)){
                Thread.yield();
            }
        }

        // On production environment you can remove this line
        // This is workaround for jmh tests. Try remove and your test hangs.
        if (Thread.interrupted())
            throw new InterruptedException();

        lastUsed.set(localIndex);
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