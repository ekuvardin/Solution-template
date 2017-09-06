package producerConsumer;

public class Worker {

    public static void main(String[] args) {

        Store store = new Store(25);
        Producer producer = new Producer(store);
        Consumer consumer = new Consumer(store);
        Consumer consumer2 = new Consumer(store);
        new Thread(producer).start();
        new Thread(consumer).start();
        new Thread(consumer2).start();
    }
}

class Producer implements Runnable {

    private Store store;

    Producer(Store store) {
        this.store = store;
    }

    public void run() {
        for (int i = 1; i < 8; i++) {
            store.put(5);
        }
    }
}

class Consumer implements Runnable {

    private Store store;

    Consumer(Store store) {
        this.store = store;
    }

    public void run() {
        for (int i = 1; i < 6; i++) {
            store.get(3);
        }
    }
}

