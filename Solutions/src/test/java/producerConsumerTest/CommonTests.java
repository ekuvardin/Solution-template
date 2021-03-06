package producerConsumerTest;

import producerConsumer.IStore;
import producerConsumerTest.framework.Consumer;
import producerConsumerTest.framework.StoreStrategy;
import producerConsumerTest.framework.Producer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public abstract class CommonTests {

    protected IStore<Integer> store;
    protected StoreStrategy<Integer> consumer = new Consumer();
    protected StoreStrategy<Integer> producer = new Producer();

    ExecutorService executor;

    @Before
    public abstract void preTest();

    @Test(timeout = 10000)
    public void storeCorrectnessTest() throws InterruptedException {
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
    public void SingleProducerSingleConsumer() throws InterruptedException, ExecutionException {
        executor = Executors.newFixedThreadPool(2);
        CompletableFuture<Integer> consumer1 = createFuture(consumer, store, 10);
        CompletableFuture<Integer> producer1 = createFuture(producer, store, 10);

        executor.shutdown();
        Assert.assertTrue(executor.awaitTermination(1, TimeUnit.MINUTES));

        Assert.assertTrue(store.IsEmpty());

        Assert.assertEquals(new Integer(0), getResults(consumer1, producer1));
    }

    @Test(timeout = 10000)
    public void SingleProducerMultipleConsumer() throws InterruptedException, ExecutionException {
        executor = Executors.newFixedThreadPool(4);
        CompletableFuture<Integer> consumer1 = createFuture(consumer, store, 10);
        CompletableFuture<Integer> consumer2 = createFuture(consumer, store, 10);
        CompletableFuture<Integer> consumer3 = createFuture(consumer, store, 10);
        CompletableFuture<Integer> producer1 = createFuture(producer, store, 30);

        executor.shutdown();
        Assert.assertTrue(executor.awaitTermination(1, TimeUnit.MINUTES));

        Assert.assertTrue(store.IsEmpty());

        Assert.assertEquals(new Integer(0), getResults(consumer1, producer1, consumer2, consumer3));
    }

    @Test(timeout = 10000)
    public void MultipleProducerMultipleConsumer() throws InterruptedException, ExecutionException {
        executor = Executors.newFixedThreadPool(4);
        CompletableFuture<Integer> consumer1 = createFuture(consumer, store, 15);
        CompletableFuture<Integer> consumer2 = createFuture(consumer, store, 5);
        CompletableFuture<Integer> producer1 = createFuture(producer, store, 10);
        CompletableFuture<Integer> producer2 = createFuture(producer, store, 10);

        executor.shutdown();
        Assert.assertTrue(executor.awaitTermination(1, TimeUnit.MINUTES));

        Assert.assertTrue(store.IsEmpty());

        Assert.assertEquals(new Integer(0), getResults(consumer1, producer1, consumer2, producer2));
    }

    @Test(timeout = 10000)
    public void HeavyLoadMultipleProducerMultipleConsumer() throws InterruptedException, ExecutionException {
        executor = Executors.newFixedThreadPool(4);
        CompletableFuture<Integer> consumer1 = createFuture(consumer, store, 10000);
        CompletableFuture<Integer> consumer2 = createFuture(consumer, store, 10000);
        CompletableFuture<Integer> producer1 = createFuture(producer, store, 10000);
        CompletableFuture<Integer> producer2 = createFuture(producer, store, 10000);

        executor.shutdown();
        Assert.assertTrue(executor.awaitTermination(1, TimeUnit.MINUTES));

        Assert.assertTrue(store.IsEmpty());

        Assert.assertEquals(new Integer(0), getResults(consumer1, producer1, consumer2, producer2));
    }

    private CompletableFuture<Integer> createFuture(StoreStrategy<Integer> strategy, IStore<Integer> store, int count) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return strategy.getResults(store, count);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }, executor);
    }

    private Integer getResults(CompletableFuture<Integer>... results) throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> res = results[0];
        for (int i = 1; i < results.length; i++) {
            res = res.thenCombineAsync(results[i], (a, b) -> a + b);
        }

        return res.get();
    }

}
