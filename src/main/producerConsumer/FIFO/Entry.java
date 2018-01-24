package main.producerConsumer.FIFO;

class Entry<T> {
    T value = null;

    public void setValue(T value) {
        this.value = value;
    }
}
