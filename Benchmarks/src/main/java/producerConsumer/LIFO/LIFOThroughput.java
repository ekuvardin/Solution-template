package producerConsumer.LIFO;


import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Control;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import producerConsumer.IStore;
import producerConsumer.IWaitStrategy;
import producerConsumer.JmhWaitStrategy;
import producerConsumer.ThreadInterruptedStrategy;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/*
    Tests analyze throughput of simple queue using LIFO strategy.
    As competitors we choose
        java.util.concurrent.LinkedBlockingDeque
        main.producerConsumer.LIFO.Store;
        main.producerConsumer.LIFO.StoreWithPark;
        main.producerConsumer.LIFO.TrickyStore;

    Note
    1. Benchmarks was running using Intel Core i502310 CPU 2.90GHZ 3.20 GHZ 4 cores
    2. Dramatically decreasing error helps calling GB before iteration.
    3. ParallelGC gives the best performance on Java9 then G1(default in Java9)

    Test 1
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 2;
    private static final int getThreads = 2;
    thread count = 4

    Benchmark                                                                                             Mode  Cnt    Score    Error   Units
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque      thrpt    5  177,472 ± 28,194  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:get  thrpt    5   88,732 ± 14,089  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:put  thrpt    5   88,741 ± 14,105  ops/ns

    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store                                  thrpt    5  141,968 ±  2,973  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:get                              thrpt    5   70,984 ±  1,486  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:put                              thrpt    5   70,983 ±  1,487  ops/ns

    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark                  thrpt    5  137,572 ± 17,214  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:get              thrpt    5   68,786 ±  8,607  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:put              thrpt    5   68,786 ±  8,607  ops/ns

    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore                      thrpt    5  159,021 ± 27,988  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:get                  thrpt    5   79,511 ± 13,997  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:put                  thrpt    5   79,510 ± 13,991  ops/ns

    Test 2
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 4;
    private static final int getThreads = 4;
    thread count = 8

    Benchmark                                                                                             Mode  Cnt    Score    Error   Units
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque      thrpt    5  108,776 ±  3,982  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:get  thrpt    5   54,731 ±  3,805  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:put  thrpt    5   54,046 ±  0,341  ops/ns

    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store                                  thrpt    5  100,654 ± 35,035  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:get                              thrpt    5   50,329 ± 17,520  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:put                              thrpt    5   50,325 ± 17,515  ops/ns

    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark                  thrpt    5  105,355 ±  2,071  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:get              thrpt    5   52,680 ±  1,035  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:put              thrpt    5   52,676 ±  1,036  ops/ns

    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore                      thrpt    5  122,004 ± 28,371  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:get                  thrpt    5   61,002 ± 14,185  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:put                  thrpt    5   61,002 ± 14,186  ops/ns


    Test 3
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 8;
    private static final int getThreads = 8;
    thread count = 16

    Benchmark                                                                                             Mode  Cnt    Score    Error   Units
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque      thrpt    5   64,959 ± 44,498  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:get  thrpt    5   32,600 ± 23,117  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:put  thrpt    5   32,359 ± 21,388  ops/ns

    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store                                  thrpt    5  105,811 ± 19,349  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:get                              thrpt    5   52,898 ±  9,650  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:put                              thrpt    5   52,912 ±  9,698  ops/ns

    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark                  thrpt    5   54,837 ±  2,045  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:get              thrpt    5   27,418 ±  1,020  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:put              thrpt    5   27,418 ±  1,025  ops/ns

    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore                      thrpt    5  134,720 ± 20,192  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:get                  thrpt    5   67,391 ± 10,184  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:put                  thrpt    5   67,330 ± 10,011  ops/ns

    Test 4(Now we try single producer multiple consumer strategy)
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 1;
    private static final int getThreads = 15;
    thread count = 16

    Benchmark                                                                                             Mode  Cnt   Score    Error   Units
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque      thrpt    5  47,866 ±  4,438  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:get  thrpt    5  23,942 ±  2,206  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:put  thrpt    5  23,924 ±  2,232  ops/ns

    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store                                  thrpt    5  28,589 ± 17,582  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:get                              thrpt    5  14,300 ±  8,781  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:put                              thrpt    5  14,289 ±  8,801  ops/ns

    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark                  thrpt    5  42,814 ±  0,960  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:get              thrpt    5  21,424 ±  0,515  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:put              thrpt    5  21,390 ±  0,449  ops/ns

    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore                      thrpt    5  43,149 ± 10,396  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:get                  thrpt    5  21,574 ±  5,190  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:put                  thrpt    5  21,575 ±  5,206  ops/ns

    Results:
    TrickyStore, Store give the best performance when equal getters or setters.
    LinkedBlockingDeque gives the best performance when total count of threads interconnecting to
    actions is quite small(less then count of cores) and in ingle producer multiple consumer strategy
 */

public class LIFOThroughput {

    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 1;
    private static final int getThreads = 15;
    private static final int threadsCount = getThreads + putThreads;


    @State(Scope.Group)
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Warmup(iterations = 4)
    @Measurement(iterations = 5)
    @Timeout(time = 5)
    @Fork(1)
    @Threads(threadsCount)
    public static class LinkedBlockingDequeBenchmarkManyPutGet {

        private LinkedBlockingDeque<Integer> simple;

        @Setup
        public void setup() throws InterruptedException {
            simple = new LinkedBlockingDeque<>(size);
        }

        @Setup(Level.Iteration)
        public void preSetup() throws InterruptedException {
            simple.clear();
        }

        @Benchmark
        @Group("LinkedBlockingDeque")
        @GroupThreads(putThreads)
        public void put() throws InterruptedException {
            simple.put(1);
        }

        @Benchmark
        @Group("LinkedBlockingDeque")
        @GroupThreads(getThreads)
        public Integer get() throws InterruptedException {
            return simple.takeLast();
        }
    }

    @State(Scope.Group)
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Warmup(iterations = 4)
    @Measurement(iterations = 5)
    @Timeout(time = 5)
    @Fork(1)
    @Threads(threadsCount)
    public static class StoreBenchmarkManyPutGet {

        private IStore<Integer> simple;
        private IWaitStrategy strategy;

        @Setup
        public void setup() throws InterruptedException {
            strategy = new JmhWaitStrategy();
            simple = new Store<>(size, Integer.class, strategy);
        }

        @Setup(Level.Iteration)
        public void preSetup(Control control) throws InterruptedException {
            ((JmhWaitStrategy)strategy).setControl(control);
            simple.clear();
        }

        @Benchmark
        @Group("Store")
        @GroupThreads(putThreads)
        public void put() throws InterruptedException {
            simple.put(1);
        }

        @Benchmark
        @Group("Store")
        @GroupThreads(getThreads)
        public Integer get() throws InterruptedException {
            return simple.get();
        }
    }

    @State(Scope.Group)
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Warmup(iterations = 4)
    @Measurement(iterations = 5)
    @Timeout(time = 5)
    @Fork(1)
    @Threads(threadsCount)
    public static class TrickyStoreBenchmarkManyPutGet {

        private IStore<Integer> simple;
        private IWaitStrategy strategy;

        @Setup
        public void setup() throws InterruptedException {
            strategy = new JmhWaitStrategy();
            simple = new TrickyStore<>(size, Integer.class, strategy);
        }

        @Setup(Level.Iteration)
        public void preSetup(Control control) throws InterruptedException {
            ((JmhWaitStrategy)strategy).setControl(control);
            simple.clear();
        }

        @Benchmark
        @Group("TrickyStore")
        @GroupThreads(putThreads)
        public void put() throws InterruptedException {
            simple.put(1);
        }

        @Benchmark
        @Group("TrickyStore")
        @GroupThreads(getThreads)
        public Integer get() throws InterruptedException {
            return simple.get();
        }
    }

    @State(Scope.Group)
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Warmup(iterations = 4)
    @Measurement(iterations = 5)
    @Timeout(time = 5)
    @Fork(1)
    @Threads(threadsCount)
    public static class StoreWithParkBenchmarkManyPutGet {

        private IStore<Integer> simple;
        private IWaitStrategy strategy;

        @Setup
        public void setup() throws InterruptedException {
            strategy = new JmhWaitStrategy();
            simple = new StoreWithPark<>(size, Integer.class, 100, strategy);
        }

        @Setup(Level.Iteration)
        public void preSetup(Control control) throws InterruptedException {
            ((JmhWaitStrategy)strategy).setControl(control);
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
                .include(LIFOThroughput.class.getSimpleName())
                .warmupIterations(4)
                .measurementIterations(5)
                .operationsPerInvocation(insert_value)
                .forks(1)
                .threads(threadsCount)
                .timeout(TimeValue.seconds(5))
                .syncIterations(true)
                .jvmArgs("-XX:+UseParallelGC")
                .shouldDoGC(true)
                .build();
        try {
            new Runner(opt).run();
        } catch (RunnerException e) {
            e.printStackTrace();
        }
    }
}
