package producerConsumerTest.LIFO;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;
import producerConsumer.IStore;
import producerConsumer.LIFO.Store;
import producerConsumer.LIFO.StoreWithPark;
import producerConsumer.ThreadInterruptedStrategy;

public class StoreWithParkTests {

    @JCStressTest(Mode.Termination)
    @Outcome(id = "TERMINATED", expect = Expect.ACCEPTABLE, desc = "Gracefully finished.")
    @Outcome(id = "STALE", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Test hung up.")
    @State
    public static class GetShouldBeUnblocked {

        volatile IStore<Integer> store = new StoreWithPark<>(2, Integer.class,100, new ThreadInterruptedStrategy());

        @Actor
        Integer actor1() throws InterruptedException {
            return store.get();
        }

        @Signal
        void signal() throws InterruptedException {
            store.put(1);
        }
    }


    @JCStressTest(Mode.Termination)
    @Outcome(id = "TERMINATED", expect = Expect.ACCEPTABLE, desc = "Gracefully finished.")
    @Outcome(id = "STALE", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Test hung up.")
    @State
    public static class GetShouldUnblockPut {

        volatile IStore<Integer> store = createObject();

        protected IStore<Integer> createObject() {
            Store<Integer> object = new StoreWithPark<>(2, Integer.class, 100, new ThreadInterruptedStrategy());

            try {
                object.put(2);
                object.put(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            return object;
        }

        @Actor
        void actor1() throws InterruptedException {
            store.put(1);
        }

        @Signal
        Integer signal() throws InterruptedException {
            return store.get();
        }
    }

    @JCStressTest
    @Outcome(id = "0", expect = Expect.FORBIDDEN, desc = "All is not fine")
    @Outcome(id = "1", expect = Expect.ACCEPTABLE, desc = "All is fine")
    @State
    public static class ParallelStorePutShouldWork {

        volatile IStore<Integer> store = new StoreWithPark<>(2, Integer.class, 100, new ThreadInterruptedStrategy());

        @Actor
        void actor1() {
            try {
                store.put(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Actor
        void actor2(I_Result result) {
            try {
                result.r1 = store.get();
            } catch (InterruptedException e) {
                result.r1 = 0;
            }
        }
    }

}
