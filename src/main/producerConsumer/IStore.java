package main.producerConsumer;

/**
 * Store representation for queue and dequeue items
 *
 * @param <T> type of stored items
 */
public interface IStore<T> {

    /**
     * Get item
     *
     * @return item
     */
    T get() throws InterruptedException;

    /**
     * Put item
     *
     * @param item putted item
     */
    void put(T item) throws InterruptedException;

    /**
     * Is empty
     *
     * @return is store empty
     */
    boolean IsEmpty();

    /**
     * Clear store
     */
    void clear() throws InterruptedException;
}
