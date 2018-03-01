package producerConsumer.FIRO;

import main.producerConsumer.FIRO.RandomStartStore;
import main.producerConsumer.IStore;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Control;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import producerConsumer.JmhWaitStrategy;

import java.util.concurrent.TimeUnit;

/*
    Tests analyze throughput of simple queue using FIRO strategy

    !!!!!!!!!!!!!!!!!WARNING!!!!!!!!!!!!!!!!!!!!!!!!
    When running many threads using single producer multiple consumer strategy and count of threads much more bigger than
    available cores the you can see that the benchmarks hangs.

    Workaround:
        try thread dump main benchmark java process
    Issue:
        I think it's because RandomStartStore use Thread.yield and never acquire lock so doesn't sleep
        Thats why main benchmarks process couldn't get process time to execute further.

    TODO:
       Use another strategy. For example CardMark

    Benchmarks was running using Intel Core i502310 CPU 2.90GHZ 3.20 GHZ 4 cores

    Test 1
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 2;
    private static final int getThreads = 2;
    thread count = 4

    Benchmark                                                                                       Mode  Cnt    Score    Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt    5  183,447 ± 10,380  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt    5   91,722 ±  5,187  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt    5   91,725 ±  5,193  ops/ns


    Test 2
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 4;
    private static final int getThreads = 4;
    thread count = 8

    Benchmark                                                                                       Mode  Cnt    Score    Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt    5  191,021 ± 17,724  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt    5   95,516 ±  8,866  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt    5   95,505 ±  8,859  ops/ns



    Test 3
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 8;
    private static final int getThreads = 8;
    thread count = 16

    Benchmark                                                                                       Mode  Cnt    Score    Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt    5  187,750 ± 28,113  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt    5   93,773 ± 14,109  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt    5   93,976 ± 14,019  ops/ns

    Test 4

    Now we try single producer multiple reader strategy

    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 1;
    private static final int getThreads = 15;
    thread count = 16

    You can see big error under get methods because 15 threads try to access 128 shared items
    and they must cyclic run on buffer trying to get not null item

    Benchmark                                                                                       Mode  Cnt    Score     Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt    5  200,455 ± 115,470  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt    5  102,444 ± 113,485  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt    5   98,010 ±  10,050  ops/ns

 */

public class FIROThroughput {

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
    @Timeout(time = 3)
    @Fork(1)
    @Threads(threadsCount)
    public static class RandomStartStoreBenchmarkManyPutGet {

        private IStore<Integer> simple;
        private JmhWaitStrategy strategy;

        @Setup(Level.Trial)
        public void setup(Control control) throws InterruptedException {
            strategy = new JmhWaitStrategy();
            simple = new RandomStartStore<>(size, strategy);
        }

        @Setup(Level.Iteration)
        public void preSetup(Control control) throws InterruptedException {
            strategy.setControl(control);
            simple.clear();
        }

        @Benchmark
        @Group("RandomStartStore")
        @GroupThreads(putThreads)
        public void put(Control cnt) throws InterruptedException {
            simple.put(1);
        }

        @Benchmark
        @Group("RandomStartStore")
        @GroupThreads(getThreads)
        public Integer get(Control cnt) throws InterruptedException {
            return simple.get();
        }
    }

    public static void main(String[] args) {

        Options opt = new OptionsBuilder()
                .include(FIROThroughput.class.getSimpleName())
                .warmupIterations(4)
                .measurementIterations(5)
                .operationsPerInvocation(insert_value)
                .forks(1)
                .threads(threadsCount)
                .timeout(TimeValue.seconds(3))
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
