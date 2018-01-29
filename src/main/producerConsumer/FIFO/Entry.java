package main.producerConsumer.FIFO;

/**
 * Holding reference class
 *
 * @param <T> type of stored items
 */
class Entry<T> {
    T value = null;


    /**
     * Set value
     *
     * @param value setting value
     */
    public void setValue(T value) {
        this.value = value;
    }
}
