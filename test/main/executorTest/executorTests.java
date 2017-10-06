package main.executorTest;

import main.executor.Executor;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class executorTests {

    Executor executor;

    @Test(timeout = 10000)
    public void OneConsumerOneTask(){
        final AtomicInteger res = new AtomicInteger(10);

        executor = new Executor(1);

        Runnable run = new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                res.decrementAndGet();
            }
        };

        for(int i=0;i<10;i++){
            executor.submit(run);
        }

        Thread
        executor.shutdownNow();
    }

    @Test
    public void OneConsumerTenTask(){

    }

    @Test
    public void TwoConsumerTenTask(){

    }

    @Test
    public void TwoConsumerTenTaskSpreadInTime(){

    }

}
