package main.executorTest;

import main.executor.Executor;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Time;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class executorTests {

    @Test(timeout = 5000)
    public void executorShouldExecuteAllTasks() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(4);

        Executor executor = new Executor(1);

        Runnable run = () -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            latch.countDown();
        };

        for (int i = 0; i < 4; i++) {
            executor.submit(run);
        }

        latch.await();
        Assert.assertEquals(0, latch.getCount());
        Assert.assertFalse(executor.getRemainingTasks().hasNext());
        executor.shutdown();
    }

    @Test(timeout = 3000)
    public void executorShouldExecuteAllTasksInParallel() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(4);

        Executor executor = new Executor(2);

        Runnable run = () -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            latch.countDown();
        };

        for (int i = 0; i < 4; i++) {
            executor.submit(run);
        }

        latch.await();
        Assert.assertEquals(0, latch.getCount());
        Assert.assertFalse(executor.getRemainingTasks().hasNext());
        executor.shutdown();
    }

    @Test(timeout = 30000)
    public void onShutdownShouldExecuteNotAllTasks() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(6);

        Executor executor = new Executor(1);

        Runnable run = () -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            latch.countDown();
        };

        for (int i = 0; i < 6; i++) {
            executor.submit(run);
        }

        Thread.sleep(2000);
        executor.shutdown();

        latch.await(10, TimeUnit.SECONDS);

        int i = 0;
        for (Iterator<Runnable> iter = executor.getRemainingTasks(); iter.hasNext(); iter.next()) {
            i++;
        }

        Assert.assertEquals(latch.getCount(), i);
    }

    @Test(timeout = 200000)
    public void awaitTerminationShouldExecuteAllTasks() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(6);

        Executor executor = new Executor(1);

        Runnable run = () -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            latch.countDown();
        };

        for (int i = 0; i < 6; i++) {
            executor.submit(run);
        }

        Thread.sleep(2000);

        Assert.assertEquals(false, executor.awaitTermination(100000));

        latch.await();
        Assert.assertEquals(true, executor.awaitTermination(1));

        int i = 0;
        for (Iterator<Runnable> iter = executor.getRemainingTasks(); iter.hasNext(); iter.next()) {
            i++;
        }

        Assert.assertEquals(latch.getCount(), i);
    }

}
