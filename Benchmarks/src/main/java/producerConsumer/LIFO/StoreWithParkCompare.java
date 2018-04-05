package producerConsumer.LIFO;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Control;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import producerConsumer.IStore;
import producerConsumer.JmhWaitStrategy;

import java.util.concurrent.TimeUnit;

/*
    In this experiment we try analyze how spin count takes influence on throughput.

    Questions:
    1. Does optimum spin count is changed when we change queue size, count of threads?

    Using processor:
    Processor Intel Core i7 3610QM 8 cores

    Let's see experiments(results in the end):

    Test 1(Try to find optimum spin count when count of producers and consumers is the same)
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 8;
    private static final int getThreads = 8;
    thread count = 16

    Now we see that optimum spin is between 1000 and 1500

    Benchmark                                                                                                   Mode  Cnt   Score    Error   Units
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark100BenchmarkManyPutGet.StoreWithPark100            thrpt    5  43,528 ±  2,633  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark100BenchmarkManyPutGet.StoreWithPark100:get        thrpt    5  21,765 ±  1,316  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark100BenchmarkManyPutGet.StoreWithPark100:put        thrpt    5  21,763 ±  1,318  ops/ns

    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark300BenchmarkManyPutGet.StoreWithPark300            thrpt    5  44,482 ±  5,774  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark300BenchmarkManyPutGet.StoreWithPark300:get        thrpt    5  22,242 ±  2,887  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark300BenchmarkManyPutGet.StoreWithPark300:put        thrpt    5  22,240 ±  2,887  ops/ns

    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark500BenchmarkManyPutGet.StoreWithPark500            thrpt    5  46,340 ± 10,326  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark500BenchmarkManyPutGet.StoreWithPark500:get        thrpt    5  23,170 ±  5,162  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark500BenchmarkManyPutGet.StoreWithPark500:put        thrpt    5  23,170 ±  5,164  ops/ns

    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark800BenchmarkManyPutGet.StoreWithPark800            thrpt    5  70,552 ± 18,412  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark800BenchmarkManyPutGet.StoreWithPark800:get        thrpt    5  35,276 ±  9,210  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark800BenchmarkManyPutGet.StoreWithPark800:put        thrpt    5  35,276 ±  9,202  ops/ns

    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark1000BenchmarkManyPutGet.StoreWithPark1000          thrpt    5  73,703 ±  4,560  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark1000BenchmarkManyPutGet.StoreWithPark1000:get      thrpt    5  36,850 ±  2,273  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark1000BenchmarkManyPutGet.StoreWithPark1000:put      thrpt    5  36,853 ±  2,288  ops/ns

    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark1500BenchmarkManyPutGet.StoreWithPark1500      	   thrpt    5  71,187 ± 22,974  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark1500BenchmarkManyPutGet.StoreWithPark1500:get  	   thrpt    5  35,594 ± 11,484  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark1500BenchmarkManyPutGet.StoreWithPark1500:put  	   thrpt    5  35,593 ± 11,490  ops/ns

    Test 2(Try to find optimum spin count when count of producers much less than consumers)
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 1;
    private static final int getThreads = 15;
    private static final int threadsCount = getThreads + putThreads;

    Now we see that changing count of getter and setters takes influence on optimum spin count.
    100 is optimum. Why? Because we have 1 producer and 15 consumer and for consumer it is better to wait instead spin because producer can't put enough values for consumers.

    Benchmark                                                                                               Mode  Cnt   Score   Error   Units
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark1000BenchmarkManyPutGet.StoreWithPark1000      thrpt    5  32,344 ± 2,440  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark1000BenchmarkManyPutGet.StoreWithPark1000:get  thrpt    5  16,175 ± 1,227  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark1000BenchmarkManyPutGet.StoreWithPark1000:put  thrpt    5  16,169 ± 1,214  ops/ns

    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark100BenchmarkManyPutGet.StoreWithPark100        thrpt    5  38,240 ± 7,918  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark100BenchmarkManyPutGet.StoreWithPark100:get    thrpt    5  19,118 ± 3,987  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark100BenchmarkManyPutGet.StoreWithPark100:put    thrpt    5  19,122 ± 3,932  ops/ns

    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark1500BenchmarkManyPutGet.StoreWithPark1500      thrpt    5  23,649 ± 5,554  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark1500BenchmarkManyPutGet.StoreWithPark1500:get  thrpt    5  11,827 ± 2,773  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark1500BenchmarkManyPutGet.StoreWithPark1500:put  thrpt    5  11,821 ± 2,781  ops/ns

    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark300BenchmarkManyPutGet.StoreWithPark300        thrpt    5  36,213 ± 3,270  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark300BenchmarkManyPutGet.StoreWithPark300:get    thrpt    5  18,107 ± 1,638  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark300BenchmarkManyPutGet.StoreWithPark300:put    thrpt    5  18,106 ± 1,632  ops/ns

    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark500BenchmarkManyPutGet.StoreWithPark500        thrpt    5  34,948 ± 1,547  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark500BenchmarkManyPutGet.StoreWithPark500:get    thrpt    5  17,474 ± 0,781  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark500BenchmarkManyPutGet.StoreWithPark500:put    thrpt    5  17,474 ± 0,767  ops/ns

    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark800BenchmarkManyPutGet.StoreWithPark800        thrpt    5  34,921 ± 1,453  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark800BenchmarkManyPutGet.StoreWithPark800:get    thrpt    5  17,468 ± 0,713  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark800BenchmarkManyPutGet.StoreWithPark800:put    thrpt    5  17,453 ± 0,745  ops/ns


    Test 3(Try to find optimum spin count when count of producers and consumers is the same and is the optimum spin correlate with thread count)
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 4;
    private static final int getThreads = 4;
    private static final int threadsCount = getThreads + putThreads;

    Now we see that optimum spin count changes to 1500(see Test 1)
    Then optimum spin count doesn't save when count of threads is changed.

    Benchmark                                                                                               Mode  Cnt   Score    Error   Units
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark1000BenchmarkManyPutGet.StoreWithPark1000      thrpt    5  89,506 ± 19,011  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark1000BenchmarkManyPutGet.StoreWithPark1000:get  thrpt    5  44,753 ±  9,504  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark1000BenchmarkManyPutGet.StoreWithPark1000:put  thrpt    5  44,753 ±  9,507  ops/ns

    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark100BenchmarkManyPutGet.StoreWithPark100        thrpt    4  86,260 ±  6,919  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark100BenchmarkManyPutGet.StoreWithPark100:get    thrpt    4  43,130 ±  3,461  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark100BenchmarkManyPutGet.StoreWithPark100:put    thrpt    4  43,130 ±  3,458  ops/ns

    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark1500BenchmarkManyPutGet.StoreWithPark1500      thrpt    5  93,555 ±  7,272  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark1500BenchmarkManyPutGet.StoreWithPark1500:get  thrpt    5  46,778 ±  3,633  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark1500BenchmarkManyPutGet.StoreWithPark1500:put  thrpt    5  46,777 ±  3,639  ops/ns

    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark300BenchmarkManyPutGet.StoreWithPark300        thrpt    5  89,748 ±  2,052  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark300BenchmarkManyPutGet.StoreWithPark300:get    thrpt    5  44,875 ±  1,026  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark300BenchmarkManyPutGet.StoreWithPark300:put    thrpt    5  44,874 ±  1,026  ops/ns

    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark500BenchmarkManyPutGet.StoreWithPark500        thrpt    5  85,509 ±  6,276  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark500BenchmarkManyPutGet.StoreWithPark500:get    thrpt    5  42,755 ±  3,138  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark500BenchmarkManyPutGet.StoreWithPark500:put    thrpt    5  42,755 ±  3,138  ops/ns

    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark800BenchmarkManyPutGet.StoreWithPark800        thrpt    5  89,320 ±  7,748  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark800BenchmarkManyPutGet.StoreWithPark800:get    thrpt    5  44,660 ±  3,874  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark800BenchmarkManyPutGet.StoreWithPark800:put    thrpt    5  44,660 ±  3,874  ops/ns

    Test 4(Does size of queue correlate with optimum spin count?)
    private static final int size = 512;
    private static final int insert_value = 10000;
    private static final int putThreads = 8;
    private static final int getThreads = 8;
    private static final int threadsCount = getThreads + putThreads;

    Now optimum spin count is 300-800(see Test 1 for compare). When change size of queue then optimum spin count is changed.

    Benchmark                                                                                               Mode  Cnt    Score    Error   Units
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark1000BenchmarkManyPutGet.StoreWithPark1000      thrpt    5  134,468 ± 15,497  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark1000BenchmarkManyPutGet.StoreWithPark1000:get  thrpt    5   67,235 ±  7,748  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark1000BenchmarkManyPutGet.StoreWithPark1000:put  thrpt    5   67,233 ±  7,749  ops/ns

    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark100BenchmarkManyPutGet.StoreWithPark100        thrpt    5  138,296 ±  4,840  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark100BenchmarkManyPutGet.StoreWithPark100:get    thrpt    5   69,148 ±  2,424  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark100BenchmarkManyPutGet.StoreWithPark100:put    thrpt    5   69,148 ±  2,416  ops/ns

    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark1500BenchmarkManyPutGet.StoreWithPark1500      thrpt    5  136,972 ±  6,059  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark1500BenchmarkManyPutGet.StoreWithPark1500:get  thrpt    5   68,487 ±  3,029  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark1500BenchmarkManyPutGet.StoreWithPark1500:put  thrpt    5   68,486 ±  3,030  ops/ns

    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark300BenchmarkManyPutGet.StoreWithPark300        thrpt    5  146,590 ± 16,266  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark300BenchmarkManyPutGet.StoreWithPark300:get    thrpt    5   73,296 ±  8,136  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark300BenchmarkManyPutGet.StoreWithPark300:put    thrpt    5   73,295 ±  8,130  ops/ns

    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark500BenchmarkManyPutGet.StoreWithPark500        thrpt    5  146,029 ± 15,971  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark500BenchmarkManyPutGet.StoreWithPark500:get    thrpt    5   73,015 ±  7,988  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark500BenchmarkManyPutGet.StoreWithPark500:put    thrpt    5   73,014 ±  7,983  ops/ns

    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark800BenchmarkManyPutGet.StoreWithPark800        thrpt    5  147,922 ± 12,301  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark800BenchmarkManyPutGet.StoreWithPark800:get    thrpt    5   73,961 ±  6,151  ops/ns
    producerConsumer.LIFO.StoreWithParkCompare.StoreWithPark800BenchmarkManyPutGet.StoreWithPark800:put    thrpt    5   73,961 ±  6,150  ops/ns

    Results:
    1. Optimum spin count changes when we change queue size and thread counts.
    2. When we have producer much less than consumer than it is better to set spin count as less as possible.
 */
public class StoreWithParkCompare {

    private static final int size = 512;
    private static final int insert_value = 10000;
    private static final int putThreads = 8;
    private static final int getThreads = 8;
    private static final int threadsCount = getThreads + putThreads;

    @State(Scope.Group)
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Warmup(iterations = 4)
    @Measurement(iterations = 5)
    @Timeout(time = 5)
    @Fork(1)
    @Threads(threadsCount)
    public static class StoreWithPark100BenchmarkManyPutGet {


        @Param({"100", "300", "500", "800", "1000", "1500"})
        private int spinCount;

        private IStore<Integer> simple;
        private JmhWaitStrategy strategy;

        @Setup
        public void setup() throws InterruptedException {
            strategy = new JmhWaitStrategy();
            simple = new StoreWithPark<>(size, Integer.class, spinCount, strategy);
        }

        @Setup(Level.Iteration)
        public void preSetup(Control control) throws InterruptedException {
            strategy.setControl(control);
            simple.clear();
        }

        @Benchmark
        @Group("StoreWithPark")
        @GroupThreads(putThreads)
        public void put() throws InterruptedException {
            simple.put(1);
        }

        @Benchmark
        @Group("StoreWithPark")
        @GroupThreads(getThreads)
        public Integer get() throws InterruptedException {
            return simple.get();
        }
    }

    public static void main(String[] args) {

        Options opt = new OptionsBuilder()
                .include(StoreWithParkCompare.class.getSimpleName())
                .warmupIterations(4)
                .measurementIterations(5)
                .operationsPerInvocation(insert_value)
                .forks(1)
                .threads(threadsCount)
                .timeout(TimeValue.seconds(5))
                .syncIterations(true)
                .jvmArgs("-ea")
                .build();
        try {
            new Runner(opt).run();
        } catch (RunnerException e) {
            e.printStackTrace();
        }
    }
}
