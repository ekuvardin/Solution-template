package producerConsumer.FIFO;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

    final ReentrantLock w;
    final Condition notEmpty;
    final Condition notFull;

    public Entry(){
        w = new ReentrantLock();;
        notEmpty = w.newCondition();
        notFull = w.newCondition();
    }

}
