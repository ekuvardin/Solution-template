package producerConsumer.FIRO;

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

import java.util.concurrent.TimeUnit;

/*
    Tests analyze throughput of simple queue using FIRO strategy

    !!!WARNING!!!
    When running many threads using single producer multiple consumer strategy and count of threads much more bigger than
    available cores the you can see Infinity ops. Try decreased count of threads or increase queue size.

    Benchmarks was running using Intel Core i502310 CPU 2.90GHZ 3.20 GHZ 4 cores

    Test 1
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 2;
    private static final int getThreads = 2;
    thread count = 4

    Benchmark                                                                                       Mode  Cnt    Score    Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt    5  184,131 ± 32,658  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt    5   92,117 ± 16,758  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt    5   92,013 ± 15,902  ops/ns


    Test 2
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 4;
    private static final int getThreads = 4;
    thread count = 8

    Benchmark                                                                                       Mode  Cnt    Score   Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt    5  178,585 ± 9,877  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt    5   89,213 ± 6,680  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt    5   89,372 ± 3,611  ops/ns

    Test 3
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 8;
    private static final int getThreads = 8;
    thread count = 16

    Benchmark                                                                                       Mode  Cnt    Score    Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt    5  175,569 ± 15,176  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt    5   87,631 ±  9,729  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt    5   87,938 ±  5,589  ops/ns

    Test 4

    Now we try single producer multiple reader strategy

    private static final int size = 512;
    private static final int insert_value = 10000;
    private static final int putThreads = 1;
    private static final int getThreads = 15;
    thread count = 16

    You can see big error under get methods because 15 threads try to access 128 shared items
    and they must cyclic run on buffer trying to get not null item

    Benchmark                                                                                       Mode  Cnt    Score    Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt    5  224,964 ± 11,756  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt    5  113,451 ± 12,467  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt    5  111,513 ±  6,173  ops/ns

 */

public class FIROThroughput {

    private static final int size = 512;
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
        private IWaitStrategy strategy;

        @Setup(Level.Trial)
        public void setup() throws InterruptedException {
            strategy = new JmhWaitStrategy();
            simple = new RandomStartStore<>(size, strategy);
        }

        @Setup(Level.Iteration)
        public void preSetup(Control cnt) throws InterruptedException {
            simple.clear();
            ((JmhWaitStrategy)strategy).setControl(cnt);
        }

        @TearDown
        @Benchmark
        @Group("RandomStartStore")
        @GroupThreads(putThreads)
        public void put() throws InterruptedException {
            simple.put(1);
        }

        @Benchmark
        @Group("RandomStartStore")
        @GroupThreads(getThreads)
        public Integer get() throws InterruptedException {
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
