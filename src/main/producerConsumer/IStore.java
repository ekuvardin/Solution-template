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
    T get();

    /**
     * Put item
     *
     * @param item putted item
     */
    void put(T item);

    /**
     * Is empty
     * @return is store empty
     */
    boolean IsEmpty();
}
