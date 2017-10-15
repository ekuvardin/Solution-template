package main.producerConsumer;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Array based on AtomicReferenceArray when several thread get's there's own
 * random index and scan sequently.
 *
 * @param <T> store elements
 */
public class RandomStartStore<T> implements IStore<T> {

    private final AtomicReferenceArray<T> array;
    private final IIndexStrategy indexStrategy;
    private final Random random = new Random();

    private ThreadLocal<Integer> lastUsed;
    private ArrayBlockingQueue<Integer> bl;

    public RandomStartStore(int size) {
        array = new AtomicReferenceArray<>(size);

        if ((size & -size) == size) {
            indexStrategy = ((p1) -> (p1 + 1) & (array.length() - 1));
        } else {
            indexStrategy = ((p1) -> (p1 + 1) % array.length());
        }

        lastUsed = ThreadLocal.withInitial(() -> random.nextInt(array.length()));
        bl = new ArrayBlockingQueue<>(size);
    }

    @Override
    public T get() throws InterruptedException {
        Integer localIndex;
        T item = null;
        for (localIndex = lastUsed.get(); item == null; localIndex = bl.take()) {
            for (int i = 0; i < array.length() && item == null; i++, localIndex = indexStrategy.getIndex(localIndex)) {
                if (array.get(localIndex) != null) {
                    item = array.getAndSet(localIndex, null);
                }

                Thread.yield();
            }
        }

        lastUsed.set(localIndex);
        return item;
    }

    @Override
    public void put(T input) throws InterruptedException {
        int localIndex = lastUsed.get();
        while (!array.compareAndSet(localIndex, null, input)) {
            localIndex = indexStrategy.getIndex(localIndex);

            Thread.yield();
        }
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
