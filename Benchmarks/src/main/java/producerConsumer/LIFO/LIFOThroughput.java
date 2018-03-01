package producerConsumer.LIFO;

import main.producerConsumer.IStore;
import main.producerConsumer.LIFO.Store;
import main.producerConsumer.LIFO.StoreWithPark;
import main.producerConsumer.LIFO.TrickyStore;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Control;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import producerConsumer.JmhWaitStrategy;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/*
    Tests analyze throughput of simple queue using LIFO strategy.
    As competitors we choose
        java.util.concurrent.LinkedBlockingDeque
        main.producerConsumer.LIFO.Store;
        main.producerConsumer.LIFO.StoreWithPark;
        main.producerConsumer.LIFO.TrickyStore;

    Benchmarks was running using Intel Core i502310 CPU 2.90GHZ 3.20 GHZ 4 cores

    Test 1
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 2;
    private static final int getThreads = 2;
    thread count = 4

    Benchmark                                                                                             Mode  Cnt    Score    Error   Units
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque      thrpt    5  181,047 ± 11,350  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:get  thrpt    5   90,524 ±  5,674  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:put  thrpt    5   90,522 ±  5,676  ops/ns

    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store                                  thrpt    5  151,207 ±  5,337  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:get                              thrpt    5   75,604 ±  2,668  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:put                              thrpt    5   75,603 ±  2,670  ops/ns

    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark                  thrpt    5  138,823 ± 15,538  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:get              thrpt    5   69,410 ±  7,769  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:put              thrpt    5   69,413 ±  7,769  ops/ns

    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore                      thrpt    5  203,216 ±  8,970  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:get                  thrpt    5  101,608 ±  4,484  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:put                  thrpt    5  101,607 ±  4,485  ops/ns


    Test 2
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 4;
    private static final int getThreads = 4;
    thread count = 8

    Benchmark                                                                                             Mode  Cnt    Score    Error   Units
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque      thrpt    5  103,966 ±  4,140  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:get  thrpt    5   51,988 ±  1,564  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:put  thrpt    5   51,979 ±  2,603  ops/ns

    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store                                  thrpt    5  110,097 ±  6,203  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:get                              thrpt    5   55,049 ±  3,102  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:put                              thrpt    5   55,048 ±  3,101  ops/ns

    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark                  thrpt    4  106,814 ± 10,921  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:get              thrpt    4   53,407 ±  5,455  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:put              thrpt    4   53,407 ±  5,465  ops/ns

    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore                      thrpt    5  141,807 ± 35,877  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:get                  thrpt    5   70,904 ± 17,939  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:put                  thrpt    5   70,903 ± 17,938  ops/ns



    Test 3
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 8;
    private static final int getThreads = 8;
    thread count = 16

    Benchmark                                                                                             Mode  Cnt    Score    Error   Units
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque      thrpt    5   61,107 ± 56,380  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:get  thrpt    5   27,551 ±  5,749  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:put  thrpt    5   33,556 ± 54,943  ops/ns

    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store                                  thrpt    5  109,811 ± 21,485  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:get                              thrpt    5   54,908 ± 10,744  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:put                              thrpt    5   54,903 ± 10,740  ops/ns

    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark                  thrpt    5   54,553 ± 10,216  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:get              thrpt    5   27,276 ±  5,101  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:put              thrpt    5   27,278 ±  5,115  ops/ns

    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore                      thrpt    5  139,450 ±  7,994  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:get                  thrpt    5   69,725 ±  3,997  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:put                  thrpt    5   69,725 ±  3,997  ops/ns

    Test 4(Now we try single producer multiple reader strategy)
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 1;
    private static final int getThreads = 15;
    thread count = 16

    Benchmark                                                                                             Mode  Cnt   Score   Error   Units
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque      thrpt    5  36,409 ± 1,876  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:get  thrpt    5  18,209 ± 0,933  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:put  thrpt    5  18,201 ± 0,943  ops/ns

    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store                                  thrpt    5  38,650 ± 5,511  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:get                              thrpt    5  19,325 ± 2,757  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:put                              thrpt    5  19,325 ± 2,754  ops/ns

    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark                  thrpt    5  46,357 ± 3,959  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:get              thrpt    5  23,172 ± 1,993  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:put              thrpt    5  23,184 ± 1,967  ops/ns

    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore                      thrpt    5  47,733 ± 2,015  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:get                  thrpt    5  23,866 ± 1,010  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:put                  thrpt    5  23,867 ± 1,005  ops/ns

    Results:
    TrickyStore gives the best throughput in all tests. When use strategy Single Producer Multiple Consumer then throughput
    decreasing dramatically and advantage TrickyStore on LinkedBlockingDeque is no more than 33%
 */

public class LIFOThroughput {

    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 2;
    private static final int getThreads = 2;
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
        private JmhWaitStrategy strategy;

        @Setup
        public void setup() throws InterruptedException {
            strategy = new JmhWaitStrategy();
            simple = new Store<>(size, Integer.class, strategy);
        }

        @Setup(Level.Iteration)
        public void preSetup(Control control) throws InterruptedException {
            strategy.setControl(control);
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
        private JmhWaitStrategy strategy;

        @Setup
        public void setup() throws InterruptedException {
            strategy = new JmhWaitStrategy();
            simple = new TrickyStore<>(size, Integer.class, strategy);
        }

        @Setup(Level.Iteration)
        public void preSetup(Control control) throws InterruptedException {
            strategy.setControl(control);
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
        private JmhWaitStrategy strategy;

        @Setup
        public void setup() throws InterruptedException {
            strategy = new JmhWaitStrategy();
            simple = new StoreWithPark<>(size, Integer.class, 100, strategy);
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
                .include(LIFOThroughput.class.getSimpleName())
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
