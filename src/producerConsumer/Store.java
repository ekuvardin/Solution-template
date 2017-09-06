package producerConsumer;

import java.util.concurrent.locks.ReentrantLock;

class Store {
    final int maxSize;
    //We needn't do this field volatile due to full fence when acquiring the Lock
    int currentSize;

    private final ReentrantLock lock = new ReentrantLock();

    public Store(int maxSize) {
        this.maxSize = maxSize;
        this.currentSize = 0;
    }

    public void get(int count) {
        while (true) {
            //Simple TTAS
            if (currentSize >= count && lock.tryLock()) {
                try {
                    // Do check on more time because currentSize may change
                    if (currentSize >= count) {
                        currentSize = currentSize - count;
                        System.out.println("Consumer buy " + count + " goods");
                        System.out.println("Goods on store: " + this.currentSize);
                        return;
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    public void put(int count) {
        while (true) {
            //Simple TTAS
            if (currentSize + count <= maxSize && lock.tryLock()) {
                try {
                    if (currentSize + count <= maxSize) {
                        currentSize = currentSize + count;
                        System.out.println("Producer add " + count + " goods");
                        System.out.println("goods on store: " + this.currentSize);
                        return;
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
    }
}
