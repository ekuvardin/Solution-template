package executorStressTests;


import main.executor.Executor;
import org.openjdk.jcstress.annotations.*;

import java.util.concurrent.CountDownLatch;

public class JStressTests {

    @JCStressTest(Mode.Termination)
    @Outcome(id = "TERMINATED", expect = Expect.ACCEPTABLE, desc = "Gracefully finished.")
    @Outcome(id = "STALE", expect = Expect.FORBIDDEN, desc = "Something go wrong. One of the task isn't executed")
    @State
    public static class AllTestMustBeExecuted {

        volatile CountDownLatch latch = new CountDownLatch(4);

        @Actor
        void actor1() throws InterruptedException {
            latch.await();
        }

        @Signal
        public void signal() throws InterruptedException {
            initExecutor(1, latch);
        }

        private void initExecutor(int workers, final CountDownLatch latch) throws InterruptedException {
            Executor executor = new Executor(workers);

            Runnable run = () -> {
                latch.countDown();
            };

            final long size = latch.getCount();
            for (int i = 0; i < size; i++) {
                executor.submit(run);
            }

            executor.shutdown();
        }
    }
}
