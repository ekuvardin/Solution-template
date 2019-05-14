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
    OS:
        Ubuntu 17.0
    Compilers:
        Oracle Java 11(Ubuntu)
        Oracle Java 10(Ubuntu)
        Zing(Ubuntu)

    Test 1
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 2;
    private static final int getThreads = 2;
    thread count = 4

    Benchmark Oracle java 10                                                                        Mode  Cnt    Score   Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt   40  198.002 ± 4.119  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt   40   99.058 ± 2.107  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt   40   98.944 ± 2.067  ops/ns

    Benchmark Oracle java 11                                                                        Mode  Cnt    Score   Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt   40  203.474 ± 4.421  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt   40  101.746 ± 2.185  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt   40  101.728 ± 2.305  ops/ns

    Benchmark Zing 19.0.2                                                                           Mode  Cnt    Score   Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt   40  182.019 ± 1.631  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt   40   91.016 ± 0.816  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt   40   91.002 ± 0.816  ops/ns

    Test 2
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 4;
    private static final int getThreads = 4;
    thread count = 8

    Benchmark Oracle java 10                                                                        Mode  Cnt    Score   Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt   40  195.363 ± 3.184  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt   40   97.688 ± 1.608  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt   40   97.675 ± 1.582  ops/ns


    Benchmark Oracle java 11                                                                        Mode  Cnt    Score   Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt   40  188.778 ± 2.840  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt   40   94.391 ± 1.432  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt   40   94.387 ± 1.414  ops/ns


    Benchmark Zing 19.0.2                                                                           Mode  Cnt    Score   Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt   40  202.113 ± 6.688  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt   40  101.042 ± 3.328  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt   40  101.071 ± 3.362  ops/ns

    Test 3
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 8;
    private static final int getThreads = 8;
    thread count = 16

    Benchmark Oracle java 10                                                                        Mode  Cnt    Score   Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt   40  198.002 ± 4.119  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt   40   99.058 ± 2.107  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt   40   98.944 ± 2.067  ops/ns

    Benchmark Oracle java 11                                                                        Mode  Cnt    Score   Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt   40  203.474 ± 4.421  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt   40  101.746 ± 2.185  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt   40  101.728 ± 2.305  ops/ns

    Benchmark Zing 19.0.2                                                                           Mode  Cnt    Score   Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt   40  219.104 ± 8.652  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt   40  109.494 ± 4.351  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt   40  109.611 ± 4.310  ops/ns

    Test 4
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 8;
    private static final int getThreads = 8;
    thread count = 16

    Benchmark Oracle java 10                                                                        Mode  Cnt    Score   Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt   40  198.002 ± 4.119  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt   40   99.058 ± 2.107  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt   40   98.944 ± 2.067  ops/ns

    Benchmark Oracle java 11                                                                        Mode  Cnt    Score   Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt   40  184.144 ± 2.002  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt   40   92.082 ± 1.008  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt   40   92.062 ± 1.121  ops/ns

    Benchmark Zing 19.0.2                                                                           Mode  Cnt    Score   Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt   40  176.456 ± 1.554  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt   40   88.043 ± 0.880  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt   40   88.413 ± 0.767  ops/ns

    Test 5

    Now we try single producer multipel reader strategy
    TODO. Try to investigate why Zing gives so worse time than Oracle Java

    private static final int size = 512;
    private static final int insert_value = 10000;
    private static final int putThreads = 1;
    private static final int getThreads = 15;
    thread count = 16

    Benchmark linux  jdk 10.0.2                                                                     Mode  Cnt   Score    Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt   40  86.020 ± 14.815  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt   40  43.477 ±  7.406  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt   40  42.544 ±  7.432  ops/ns


    Benchmark linux  jdk 11.0.2                                                                     Mode  Cnt   Score    Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt   40  111.365 ± 15.214  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt   40   56.333 ±  7.669  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt   40   55.033 ±  7.570  ops/ns

    Benchmark linux Zing 19.0.2                                                                     Mode  Cnt   Score   Error   Units
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore      thrpt   40  61.793 ± 6.553  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:get  thrpt   40  31.220 ± 3.311  ops/ns
    producerConsumer.FIRO.FIROThroughput.RandomStartStoreBenchmarkManyPutGet.RandomStartStore:put  thrpt   40  30.573 ± 3.246  ops/ns

 */

public class FIROThroughput {

    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 8;
    private static final int getThreads = 8;
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

        private IStore<Tested> simple;
        private IWaitStrategy strategy;

        @State(Scope.Thread)
        public static class Tested {
            public byte data[] = new byte[1000];
        }

        @Setup(Level.Trial)
        public void setup() {
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
            simple.put(new Tested());
        }

        @Benchmark
        @Group("RandomStartStore")
        @GroupThreads(getThreads)
        public Tested get() throws InterruptedException {
            return simple.get();
        }
    }

    public static void main(String[] args) {

        Options opt = new OptionsBuilder()
                .include(FIROThroughput.class.getSimpleName())
                .warmupIterations(30)
                .measurementIterations(20)
                .operationsPerInvocation(insert_value)
                .forks(1)
                .threads(threadsCount)
                .timeout(TimeValue.seconds(5))
                .syncIterations(true)
                .jvmArgs("-server","-Xms1024m", "-Xmx1024m")
           //  .jvmArgs("-XX:+UseC2")
           //   .jvmArgs("-XX:+UseParallelGC")
           //     .jvmArgs("-XX:+UseFalcon")
                .build();
        try {
            new Runner(opt).run();
        } catch (RunnerException e) {
            e.printStackTrace();
        }
    }
}
