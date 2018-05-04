package producerConsumer.FIFO;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import producerConsumer.IStore;
import producerConsumer.IWaitStrategy;
import producerConsumer.ThreadInterruptedStrategy;

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

    Benchmarks was running using Intel Core i502310 CPU 2.90GHZ 3.20 GHZ 4 cores Windows7

    Test 1
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 2;
    private static final int getThreads = 2;
    thread count = 4

    As we see that ArrayBlockingQueue is the winner.

    Benchmark Oracle 10                                                                                         Mode  Cnt    Score    Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt   15  107.442 ±  7.148  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt   15   53.719 ±  3.576  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt   15   53.724 ±  3.572  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore      thrpt   15  107.597 ±  1.372  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:get  thrpt   15   53.797 ±  0.689  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:put  thrpt   15   53.800 ±  0.685  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore                        thrpt   15  130.435 ± 33.442  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:get                    thrpt   15   65.236 ± 16.732  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:put                    thrpt   15   65.200 ± 16.711  ops/ns


    Benchmark Zing Falcon                                                                                      Mode  Cnt    Score    Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt   15  130.858 ± 12.873  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt   15   65.437 ±  6.453  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt   15   65.421 ±  6.421  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore      thrpt   15  117.289 ±  3.589  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:get  thrpt   15   58.660 ±  1.814  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:put  thrpt   15   58.629 ±  1.776  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore                        thrpt   15  100.386 ±  2.634  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:get                    thrpt   15   50.197 ±  1.230  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:put                    thrpt   15   50.189 ±  1.414  ops/ns

    Benchmark Zing C2                                                                                           Mode  Cnt    Score   Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt   15  119.546 ± 1.792  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt   15   59.773 ± 0.896  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt   15   59.773 ± 0.896  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore      thrpt   15  116.583 ± 3.698  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:get  thrpt   15   58.284 ± 1.834  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:put  thrpt   15   58.298 ± 1.864  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore                        thrpt   15   58.362 ± 3.455  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:get                    thrpt   15   29.176 ± 1.722  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:put                    thrpt   15   29.186 ± 1.733  ops/ns
    Test 2
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 4;
    private static final int getThreads = 4;
    thread count = 8

    ArrayBlockingQueue and CyclicLockOnEntryStore have near the same throughput,
    but as TwoLocksStore uses 2 locks then CPU is not grab all available potential.

    Benchmark Oracle 10                                                                                         Mode  Cnt    Score    Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt   15   58.401 ±  1.754  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt   15   28.955 ±  0.715  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt   15   29.446 ±  1.212  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore      thrpt   15  110.023 ±  1.189  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:get  thrpt   15   55.040 ±  0.567  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:put  thrpt   15   54.983 ±  0.629  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore                        thrpt   15  138.910 ± 29.763  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:get                    thrpt   15   69.481 ± 14.896  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:put                    thrpt   15   69.429 ± 14.867  ops/ns

    Benchmark Zing Falcon                                                                                       Mode  Cnt    Score    Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt   15   77.849 ±  8.019  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt   15   39.059 ±  3.893  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt   15   38.790 ±  4.141  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore      thrpt   15  115.952 ±  1.686  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:get  thrpt   15   57.972 ±  0.860  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:put  thrpt   15   57.980 ±  0.831  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore                        thrpt   15  121.515 ± 27.718  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:get                    thrpt   15   60.745 ± 13.857  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:put                    thrpt   15   60.770 ± 13.863  ops/ns

    Benchmark Zing C2                                                                                           Mode  Cnt    Score   Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt   15   61.241 ± 4.796  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt   15   30.514 ± 2.836  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt   15   30.727 ± 2.626  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore      thrpt   15  112.618 ± 5.061  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:get  thrpt   15   56.283 ± 2.510  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:put  thrpt   15   56.336 ± 2.553  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore                        thrpt   15   60.571 ± 3.898  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:get                    thrpt   15   30.290 ± 1.913  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:put                    thrpt   15   30.281 ± 1.988  ops/ns

    Test 3
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 8;
    private static final int getThreads = 8;
    thread count = 16

    Now ArrayBlockingQueue gives the worst performance and others give the same throughput

    Benchmark Oracle 10                                                                                         Mode  Cnt    Score    Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt   15   38.116 ± 12.470  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt   15   19.077 ±  6.864  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt   15   19.040 ±  5.661  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore      thrpt   15  108.586 ±  2.053  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:get  thrpt   15   54.308 ±  1.061  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:put  thrpt   15   54.278 ±  0.997  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore                        thrpt   15  102.388 ± 11.617  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:get                    thrpt   15   51.197 ±  5.808  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:put                    thrpt   15   51.191 ±  5.809  ops/ns

    Benchmark Zing Falcon                                                                                       Mode  Cnt    Score    Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt   15   41.900 ±  6.867  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt   15   21.090 ±  3.237  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt   15   20.810 ±  3.823  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore      thrpt   15  117.403 ±  1.922  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:get  thrpt   15   58.721 ±  1.002  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:put  thrpt   15   58.682 ±  0.930  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore                        thrpt   15  127.757 ± 33.179  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:get                    thrpt   15   63.901 ± 16.587  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:put                    thrpt   15   63.857 ± 16.592  ops/ns

    Benchmark Zing C2                                                                                           Mode  Cnt    Score    Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt   15   61.610 ± 35.292  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt   15   26.996 ± 13.721  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt   15   34.614 ± 35.958  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore      thrpt   15  111.475 ±  5.734  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:get  thrpt   15   55.706 ±  2.890  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:put  thrpt   15   55.769 ±  2.848  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore                        thrpt   15   66.163 ±  4.048  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:get                    thrpt   15   33.095 ±  2.037  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:put                    thrpt   15   33.068 ±  2.017  ops/ns

    Test 4
    Now we try single producer multiple reader strategy which is used in many paradigm of programming(For ex see Disruptor)

    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 1;
    private static final int getThreads = 15;
    thread count = 16

    Benchmark Oracle 10                                                                                         Mode  Cnt    Score    Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt   15   14.907 ±  2.490  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt   15    7.462 ±  1.247  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt   15    7.445 ±  1.244  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore      thrpt   15  113.975 ±  8.635  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:get  thrpt   15   57.155 ±  4.280  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:put  thrpt   15   56.820 ±  4.363  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore                        thrpt   15   57.709 ± 25.343  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:get                    thrpt   15   29.064 ± 12.784  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:put                    thrpt   15   28.645 ± 12.568  ops/ns

    Benchmark Zing Falcon                                                                                       Mode  Cnt    Score    Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt   15   19.637 ±  2.794  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt   15    9.876 ±  1.477  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt   15    9.760 ±  1.348  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore      thrpt   15  116.015 ±  2.349  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:get  thrpt   15   58.086 ±  1.237  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:put  thrpt   15   57.929 ±  1.154  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore                        thrpt   15   38.167 ± 19.595  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:get                    thrpt   15   19.175 ±  9.871  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:put                    thrpt   15   18.992 ±  9.725  ops/ns

    Benchmark Zing C2                                                                                           Mode  Cnt    Score   Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt   15   11.628 ± 1.019  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt   15    5.856 ± 0.570  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt   15    5.772 ± 0.451  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore      thrpt   15  113.147 ± 2.222  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:get  thrpt   15   56.544 ± 1.151  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:put  thrpt   15   56.604 ± 1.093  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore                        thrpt   15   24.203 ± 6.364  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:get                    thrpt   15   12.107 ± 3.024  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:put                    thrpt   15   12.096 ± 3.352  ops/ns

 */

public class FIFOThroughput {

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
        public void preSetup() {
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
        public void setup() {
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
        public void setup() {
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
        public void put() throws InterruptedException {
            simple.put(1);
        }

        @Benchmark
        @Group("TwoLocksStore")
        @GroupThreads(getThreads)
        public Integer get() throws InterruptedException {
            return simple.get();
        }
    }

    public static void main(String[] args) {

        Options opt = new OptionsBuilder()
                .include(FIFOThroughput.class.getSimpleName())
                .warmupIterations(6)
                .measurementIterations(5)
                .operationsPerInvocation(insert_value)
                .forks(3)
                .threads(threadsCount)
                .timeout(TimeValue.seconds(3))
                 // .jvmArgs("-XX:+UseC2")
                 // .jvmArgs("-XX:+UseFalcon")
                 // .jvmArgs("-XX:+UseParallelGC")
                .build();
        try {
            new Runner(opt).run();
        } catch (RunnerException e) {
            e.printStackTrace();
        }
    }
}