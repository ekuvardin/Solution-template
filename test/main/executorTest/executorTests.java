package main.executorTest;

import main.executor.Executor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class executorTests {

    private CountDownLatch latch;
    private Executor executor;

    @Before
    public void preInitTest(){
        latch = new CountDownLatch(4);
    }

    @After
    public void postTest(){
        executor.shutdown();
        if(!executor.awaitTermination(2)){
            executor.shutdownNow();
        }
    }

    @Test(timeout = 5000)
    public void executorShouldExecuteAllTasks() throws InterruptedException {
        initExecutor(latch, 1);

        latch.await();
        checkRemainingTasks(executor);

        executor.shutdown();
    }

    @Test(timeout = 4000)
    public void executorShouldExecuteAllTasksInParallel() throws InterruptedException {
        initExecutor(latch, 2);

        latch.await();
        checkRemainingTasks(executor);

        executor.shutdown();
    }

    @Test(timeout = 5000)
    public void shutdownShouldExecuteNotAllTasks() throws InterruptedException {
        initExecutor(latch, 1);

        executor.shutdown();

        Assert.assertTrue(latch.getCount() > 0);
    }

    @Test(timeout = 15000)
    public void shutdownShouldExecuteAllTasks() throws InterruptedException {
        initExecutor(latch, 1);

        executor.shutdown();
        latch.await(10, TimeUnit.SECONDS);

        checkRemainingTasks(executor);
    }

    @Test(timeout = 20000)
    public void awaitTerminationShouldNotExecuteAllTasks() throws InterruptedException {
        initExecutor(latch, 1);

        // All task can't execute so fast
        Assert.assertEquals(false, executor.awaitTermination(1));
        executor.shutdown();
    }

    @Test(timeout = 10000)
    public void awaitTerminationShouldExecuteAllTasks() throws InterruptedException {
        initExecutor(latch, 1);

        latch.await();

        Assert.assertEquals(true, executor.awaitTermination(1));
        checkRemainingTasks(executor);
    }

    @Test(timeout = 20000)
    public void shutdownNowShouldNotExecuteAllTasks() throws InterruptedException {
        initExecutor(latch, 1);

        executor.shutdownNow();

        latch.await(10, TimeUnit.SECONDS);

        Assert.assertTrue(latch.getCount() > 0);
        checkRemainingTasks(executor);
    }

    private void initExecutor(final CountDownLatch latch, int workers) throws InterruptedException {
        executor = new Executor(workers);

        Runnable run = () -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            latch.countDown();
        };

        final long size = latch.getCount();
        for (int i = 0; i < size; i++) {
            executor.submit(run);
        }

        Thread.sleep(2000);
    }

    private void checkRemainingTasks(Executor executor){
        int i = 0;
        for (Iterator<Runnable> iter = executor.getRemainingTasks(); iter.hasNext(); iter.next()) {
            i++;
        }

        Assert.assertEquals(latch.getCount(), i);
    }
}
