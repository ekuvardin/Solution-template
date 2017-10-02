package main.producerConsumerTest;

import main.producerConsumer.IStore;
import main.producerConsumerTest.framework.Consumer;
import main.producerConsumerTest.framework.Producer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class CommonTests {

    protected IStore<Integer> store;

    @Before
    public abstract void preTest();

    @Test(timeout = 10000)
    public void storeCorrectnessTest() {
        List<Integer> tmp = new ArrayList<>(5);

        for (int i = 0; i < 5; i++) {
            tmp.add(i);
            store.put(i);
        }

        for (int i = 0; i < 5; i++) {
            tmp.remove(store.get());
        }

        Assert.assertTrue(tmp.isEmpty());
    }

    @Test(timeout = 10000)
    public void SingleProducerSingleConsumer() throws InterruptedException {
        Producer producer = new Producer(store, 10);
        Consumer consumer = new Consumer(store, 10);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(producer);
        executor.submit(consumer);

        executor.shutdown();
        Assert.assertTrue(executor.awaitTermination(1, TimeUnit.MINUTES));

        Assert.assertTrue(store.IsEmpty());
        Assert.assertEquals(0, producer.getCount());
        Assert.assertEquals(0, consumer.getCount());
    }

    @Test(timeout = 10000)
    public void SingleProducerMultipleConsumer() throws InterruptedException {
        Producer producer = new Producer(store, 60);
        Consumer consumer1 = new Consumer(store, 20);
        Consumer consumer2 = new Consumer(store, 20);
        Consumer consumer3 = new Consumer(store, 20);

        ExecutorService executor = Executors.newFixedThreadPool(4);
        executor.submit(producer);
        executor.submit(consumer1);
        executor.submit(consumer2);
        executor.submit(consumer3);

        executor.shutdown();
        Assert.assertTrue(executor.awaitTermination(1, TimeUnit.MINUTES));

        Assert.assertTrue(store.IsEmpty());
        Assert.assertEquals(0, producer.getCount());
        Assert.assertEquals(0, consumer1.getCount());
        Assert.assertEquals(0, consumer2.getCount());
        Assert.assertEquals(0, consumer3.getCount());
    }

    @Test(timeout = 10000)
    public void MultipleProducerMultipleConsumer() throws InterruptedException {
        Producer producer1 = new Producer(store, 10);
        Producer producer2 = new Producer(store, 10);
        Consumer consumer1 = new Consumer(store, 5);
        Consumer consumer2 = new Consumer(store, 15);

        ExecutorService executor = Executors.newFixedThreadPool(4);
        executor.submit(producer1);
        executor.submit(producer2);
        executor.submit(consumer1);
        executor.submit(consumer2);

        executor.shutdown();
        Assert.assertTrue(executor.awaitTermination(1, TimeUnit.MINUTES));

        Assert.assertTrue(store.IsEmpty());
        Assert.assertEquals(0, producer1.getCount());
        Assert.assertEquals(0, producer2.getCount());
        Assert.assertEquals(0, consumer1.getCount());
        Assert.assertEquals(0, consumer2.getCount());
    }

}
