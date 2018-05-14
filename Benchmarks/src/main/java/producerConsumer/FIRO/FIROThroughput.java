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

    Windows 7 Oracle java 8
    Benchmark                                                                                       Mode  Cnt    Score    Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt    5  184,131 ± 32,658  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt    5   92,117 ± 16,758  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt    5   92,013 ± 15,902  ops/ns

    Ubuntu 17 Oracle Java 10
    Benchmark                                                                                       Mode  Cnt    Score    Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt    5  191.617 ± 12.091  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt    5   95.794 ±  6.047  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt    5   95.823 ±  6.047  ops/ns

    Ubuntu 17 Zing Falcon
    Benchmark                                                                                       Mode  Cnt    Score    Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt    5  178.591 ± 14.678  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt    5   89.252 ±  7.410  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt    5   89.339 ±  7.292  ops/ns

    Ubuntu 17 Zing C2
    Benchmark                                                                                       Mode  Cnt    Score   Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt    5  168.649 ± 5.998  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt    5   84.343 ± 3.070  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt    5   84.306 ± 2.935  ops/ns

    Test 2
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 4;
    private static final int getThreads = 4;
    thread count = 8

    Windows 7 Oracle java 8
    Benchmark                                                                                       Mode  Cnt    Score   Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt    5  178,585 ± 9,877  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt    5   89,213 ± 6,680  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt    5   89,372 ± 3,611  ops/ns

    Ubuntu 17 Oracle Java 10
    Benchmark                                                                                       Mode  Cnt    Score   Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt    5  198.661 ± 2.833  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt    5   99.386 ± 2.034  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt    5   99.276 ± 1.395  ops/ns

    Ubuntu 17 Zing Falcon
    Benchmark                                                                                       Mode  Cnt    Score    Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt    5  200.120 ± 21.536  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt    5  100.195 ± 10.777  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt    5   99.925 ± 10.779  ops/ns

    Ubuntu 17 Zing C2
    Benchmark                                                                                       Mode  Cnt   Score    Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt    5  64.075 ± 11.845  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt    5  32.050 ±  5.682  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt    5  32.025 ±  6.174  ops/ns

    Test 3
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 8;
    private static final int getThreads = 8;
    thread count = 16

    Windows 7 Oracle java 8
    Benchmark                                                                                       Mode  Cnt    Score    Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt    5  216.577 ± 79.261  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt    5  108.305 ± 39.677  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt    5  108.272 ± 39.626  ops/ns

    Ubuntu 17 Oracle java 10
    Benchmark                                                                                       Mode  Cnt    Score   Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt    5  181.989 ± 5.140  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt    5   91.210 ± 3.413  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt    5   90.779 ± 2.610  ops/ns

    Ubuntu 17 Zing C2
    Benchmark                                                                                       Mode  Cnt   Score   Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt    5  64.355 ± 3.562  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt    5  32.167 ± 1.749  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt    5  32.188 ± 1.909  ops/ns

    Ubuntu 17 Zing Falcon
    Benchmark                                                                                       Mode  Cnt    Score    Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt    5  204.120 ± 15.058  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt    5  102.035 ±  7.253  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt    5  102.085 ±  7.887  ops/ns

    Test 4

    Now we try single producer multiple reader strategy
    TODO. Try to investigate why Zink gives worse time than Oracle Java

    private static final int size = 512;
    private static final int insert_value = 10000;
    private static final int putThreads = 1;
    private static final int getThreads = 8;
    thread count = 16

    Benchmark                                                                                       Mode  Cnt    Score    Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt    5  224,964 ± 11,756  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt    5  113,451 ± 12,467  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt    5  111,513 ±  6,173  ops/ns

    Benchmark Oracle 10 Linux                                                                                       Mode  Cnt    Score    Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt   15  114.572 ± 28.406  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt   15   57.829 ± 14.170  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt   15   56.743 ± 14.280  ops/ns

    Falcon
    Benchmark                                                                                       Mode  Cnt   Score    Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt    5  46.915 ± 22.778  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt    5  23.872 ± 11.472  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt    5  23.044 ± 11.398  ops/ns

    C2
    Benchmark                                                                                       Mode  Cnt   Score   Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt    5  23.005 ± 6.465  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt    5  11.627 ± 3.220  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt    5  11.379 ± 3.256  ops/ns

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
                .warmupIterations(40)
                .measurementIterations(5)
                .operationsPerInvocation(insert_value)
                .forks(1)
                .threads(threadsCount)
                .timeout(TimeValue.seconds(3))
                .syncIterations(true)
           //  .jvmArgs("-XX:+UseC2")
            .jvmArgs("-XX:+UseParallelGC")
           //     .jvmArgs("-XX:+UseFalcon")
                .build();
        try {
            new Runner(opt).run();
        } catch (RunnerException e) {
            e.printStackTrace();
        }
    }
}
