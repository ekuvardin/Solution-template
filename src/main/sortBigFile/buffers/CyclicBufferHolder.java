package main.sortBigFile.buffers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Class for holding collection of cyclic arrays. It is container for holding cyclic arrays.
 * Threads may concurrently get buffers and after work have to return it to CyclicBufferHolder
 *
 * @param <T> type of sorting elements
 */
public class CyclicBufferHolder<T> {

    private final List<ICyclicBuffer<T>> list;
    private volatile int size;

    public CyclicBufferHolder(T[] array, int chunks) {
        list = new ArrayList<>(chunks);

        int chunk = array.length / chunks;
        for (int i = 0, start = 0; i < chunks; i++) {
            final int end = (i == chunks - 1) ? array.length : start + chunk;
            list.add(new CyclicBuffer<>(array, start, end));
            start = end;
        }

        size = chunks;
    }

    /**
     * Get count number of ICyclicBuffer arrays
     *
     * @param count number of arrays to be requested
     * @return List of ICyclicBuffer arrays
     */
    public List<ICyclicBuffer<T>> getCyclicBuffer(int count) {
        while (true) {
            if (size >= count) {
                synchronized (this) {
                    if (size >= count) {
                        List<ICyclicBuffer<T>> res = new ArrayList<>(list.subList(0, count));
                        list.removeAll(res);
                        size = size - count;
                        return res;
                    }
                }
            }
        }
    }

    /**
     * Return arrays back to holder
     *
     * @param buffers List of returning arrays
     */
    public synchronized void putCyclicBuffer(Collection<ICyclicBuffer<T>> buffers) {
        list.addAll(buffers);
        size = size + buffers.size();
    }

    /**
     * Return array back to holder
     *
     * @param val returning array
     */
    public synchronized void putCyclicBuffer(ICyclicBuffer<T> val) {
        list.add(val);
        size++;
    }

    /**
     * Get count of buffers
     *
     * @return count of buffers
     */
    public int getSize() {
        return size;
    }

    /**
     * Simple cyclic buffer array
     *
     * @param <T> type of elements in array
     */
    static class CyclicBuffer<T> implements ICyclicBuffer<T> {
        final T array[];
        final int startPointer;
        final int capacity;
        final IIndexStrategy indexStrategy;

        int head;
        int tail;

        private CyclicBuffer(T[] array, int startPointer, int endPointer) {
            this.array = array;
            this.startPointer = startPointer;
            this.capacity = endPointer - startPointer;

            if ((this.capacity & -this.capacity) == this.capacity) {
                indexStrategy = ((p1) -> p1 & (this.capacity - 1));
            } else {
                indexStrategy = ((p1) -> p1 % this.capacity);
            }
        }

        @Override
        public T getFirst() {
            if (getSize() == 0) {
                throw new NoSuchElementException();
            }

            return array[indexStrategy.getIndex(head) + startPointer];
        }

        @Override
        public T getLast() {
            if (getSize() == 0) {
                throw new NoSuchElementException();
            }
            return array[indexStrategy.getIndex(tail - 1) + startPointer];
        }

        @Override
        public int getSize() {
            return tail - head;
        }

        @Override
        public int getCapacity() {
            return capacity;
        }

        @Override
        public void put(T value) {
            if (getSize() == capacity) {
                throw new RuntimeException();
            }

            array[indexStrategy.getIndex(tail) + startPointer] = value;
            tail++;
        }

        @Override
        public T pull() {
            T value = getFirst();
            array[indexStrategy.getIndex(head) + startPointer] = null;//Let's GC do it's work
            head++;

            return value;
        }

        @Override
        public void reset() {
            head = tail = 0;
        }

        /**
         * Define how we identify element in array
         */
        @FunctionalInterface
        protected interface IIndexStrategy {
            int getIndex(int p1);
        }
    }
}