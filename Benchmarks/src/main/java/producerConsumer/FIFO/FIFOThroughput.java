package producerConsumer.FIFO;

import main.producerConsumer.FIFO.CyclicLockOnEntryStore;
import main.producerConsumer.FIFO.TwoLocksStore;
import main.producerConsumer.IStore;
import main.producerConsumer.IWaitStrategy;
import main.producerConsumer.ThreadInterruptedStrategy;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Control;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/*
    Tests analyze throughput of simple queue using FIFO strategy.
    As competitors we choose
        java.util.concurrent.ArrayBlockingQueue
        main.producerConsumer.FIFO.CyclicLockOnEntryStore;
        main.producerConsumer.FIFO.TwoLocksStore;

    Some observation
    When we have count of threads equals to cores in processors then DL ArrayBlockingQueue gives the best throughput. But when count of threads is increasing
    then ArrayBlockingQueue gives worse results. Take a look of benchmarks below

    Benchmarks was running using Intel Core i502310 CPU 2.90GHZ 3.20 GHZ 4 cores

    Test 1
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 2;
    private static final int getThreads = 2;
    thread count = 4

    As we see that ArrayBlockingQueue is the winner.

    Benchmark                                                                                                   Mode  Cnt    Score    Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt    5  196,606 ± 32,189  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt    5   98,303 ± 16,095  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt    5   98,303 ± 16,094  ops/ns

    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore      thrpt    5  110,848 ±  4,018  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:get  thrpt    5   55,425 ±  2,018  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:put  thrpt    5   55,423 ±  2,000  ops/ns

    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore                        thrpt    5   81,579 ± 14,028  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:get                    thrpt    5   40,791 ±  6,998  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:put                    thrpt    5   40,788 ±  7,030  ops/ns


    Test 2
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 4;
    private static final int getThreads = 4;
    thread count = 8

    ArrayBlockingQueue and CyclicLockOnEntryStore have near the same throughput,
    but as TwoLocksStore uses 2 locks then CPU is not grab all available potential.

    Benchmark                                                                                                   Mode  Cnt    Score    Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt    5  109,419 ±  6,614  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt    5   54,629 ±  3,595  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt    5   54,790 ±  3,131  ops/ns

    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore      thrpt    5  107,693 ±  3,577  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:get  thrpt    5   53,857 ±  1,791  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:put  thrpt    5   53,836 ±  1,790  ops/ns

    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore                        thrpt    5  142,484 ± 33,333  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:get                    thrpt    5   70,445 ± 22,203  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:put                    thrpt    5   72,040 ± 14,298  ops/ns

    Test 3
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 8;
    private static final int getThreads = 8;
    thread count = 16

    Now ArrayBlockingQueue gives the worst performance and others give the same throughput

    Benchmark                                                                                                   Mode  Cnt    Score    Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt    5   58,641 ± 13,000  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt    5   29,515 ±  7,970  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt    5   29,126 ±  5,619  ops/ns

    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore      thrpt    5  122,147 ± 4,345  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:get  thrpt    5   61,014 ± 1,945  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:put  thrpt    5   61,133 ± 2,594  ops/ns

    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore                        thrpt    5  122,209 ± 9,916  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:get                    thrpt    5   61,099 ± 4,951  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:put                    thrpt    5   61,110 ± 4,965  ops/ns

    Test 4
    Now we try single producer multiple reader strategy which is used in many paradigm of programming(For ex see Disruptor)

    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 1;
    private static final int getThreads = 15;
    thread count = 16

    Benchmark
                            Mode  Cnt    Score   Error   Units
producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.Array
BlockingQueue              thrpt    5   41,793 ? 3,600  ops/ns
producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.Array
BlockingQueue:get          thrpt    5   20,908 ? 1,819  ops/ns
producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.Array
BlockingQueue:put          thrpt    5   20,886 ? 1,780  ops/ns
producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.C
yclicLockOnEntryStore      thrpt    5  131,026 ? 5,499  ops/ns
producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.C
yclicLockOnEntryStore:get  thrpt    5   65,664 ? 3,270  ops/ns
producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.C
yclicLockOnEntryStore:put  thrpt    5   65,362 ? 2,782  ops/ns
producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksSt
ore                        thrpt    5  137,547 ? 1,852  ops/ns
producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksSt
ore:get                    thrpt    5   68,911 ? 2,216  ops/ns
producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksSt
ore:put                    thrpt    5   68,636 ? 0,909  ops/ns

 */

public class FIFOThroughput {

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
    @Fork(1)
    @Timeout(time = 3)
    @Threads(threadsCount)
    @OperationsPerInvocation(insert_value)
    public static class ArrayBlockingQueueBenchmarkManyPutGet {

        private ArrayBlockingQueue<Integer> arrayBlockingQueue;

        @Setup
        public void setup() {
            arrayBlockingQueue = new ArrayBlockingQueue<>(size);
        }

        @Setup(Level.Iteration)
        public void preSetup() throws InterruptedException {
            arrayBlockingQueue.clear();
        }

        @Benchmark
        @Group("ArrayBlockingQueue")
        @GroupThreads(putThreads)
        public void put() throws InterruptedException {
            arrayBlockingQueue.put(1);
        }

        @Benchmark
        @Group("ArrayBlockingQueue")
        @GroupThreads(getThreads)
        public Integer get() throws InterruptedException {
            return arrayBlockingQueue.take();
        }
    }

    @State(Scope.Group)
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    @Warmup(iterations = 4)
    @Measurement(iterations = 5)
    @Timeout(time = 3)
    @Fork(1)
    @Threads(threadsCount)
    @OperationsPerInvocation(insert_value)
    public static class CyclicLockOnEntryStoreBenchmarkManyPutGet {

        private IStore<Integer> simple;
        private IWaitStrategy strategy;

        @Setup
        public void setup() throws InterruptedException {
            strategy = new ThreadInterruptedStrategy();
            simple = new CyclicLockOnEntryStore<>(size, strategy);
        }

        @Setup(Level.Iteration)
        public void preSetup() throws InterruptedException {
            simple.clear();
        }

        @Benchmark
        @Group("CyclicLockOnEntryStore")
        @GroupThreads(putThreads)
        public void put() throws InterruptedException {
            simple.put(1);
        }

        @Benchmark
        @Group("CyclicLockOnEntryStore")
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
    @Timeout(time = 3)
    @Fork(1)
    @Threads(threadsCount)
    @OperationsPerInvocation(insert_value)
    public static class TwoLocksStoreBenchmarkManyPutGet {

        private IStore<Integer> simple;
        private IWaitStrategy strategy;

        @Setup
        public void setup() throws InterruptedException {
            strategy = new ThreadInterruptedStrategy();
            simple = new TwoLocksStore<>(size, strategy);
        }

        @Setup(Level.Iteration)
        public void preSetup() throws InterruptedException {
            simple.clear();
        }

        @Benchmark
        @Group("TwoLocksStore")
        @GroupThreads(putThreads)
        public void put(Control control) throws InterruptedException {
            simple.put(1);
        }

        @Benchmark
        @Group("TwoLocksStore")
        @GroupThreads(getThreads)
        public Integer get(Control control) throws InterruptedException {
            return simple.get();
        }
    }

    public static void main(String[] args) {

        Options opt = new OptionsBuilder()
                .include(FIFOThroughput.class.getSimpleName())
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
