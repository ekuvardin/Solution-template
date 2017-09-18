package main.sortBigFile.buffers;

/**
 * Interface describing behavior of cyclic array
 *
 * @param <T> type of sorting elements
 */
public interface ICyclicBuffer<T extends Comparable<T>> {

    /**
     * Return first element in array
     *
     * @return first element
     */
    T getFirst();

    /**
     * Return last element in array
     *
     * @return last element
     */
    T getLast();

    /**
     * Return current array size
     *
     * @return current array size
     */
    int getSize();

    /**
     * Return current array max capacity
     *
     * @return current array max capacity
     */
    int getCapacity();

    /**
     * Put element in array to the tail
     *
     * @param value element
     */
    void put(T value);

    /**
     * Get first element in array and remove it
     *
     * @return first element in array
     */
    T pull();

    /**
     * Reset all pointers in buffer
     *
     */
    void reset();
}