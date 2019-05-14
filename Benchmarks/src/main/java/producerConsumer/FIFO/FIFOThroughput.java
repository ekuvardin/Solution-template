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

    Benchmarks was running using Intel Core i502310 CPU 2.90GHZ 3.20 GHZ 4 cores
    OS:
        Windows7
        Ubuntu 17.0
    Compilers:
        Oracle Java 9(WS7)
        Oracle Java 10(Ubuntu)
        Zing(Ubuntu)

    Test 1
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 2;
    private static final int getThreads = 2;
    thread count = 4

    As we see that ArrayBlockingQueue is the winner no matter what compiler is.

    Benchmark Oracle 9 Windows                                                                                  Mode  Cnt    Score    Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt    5  199,597 ±  3,696  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt    5   99,797 ±  1,837  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt    5   99,800 ±  1,863  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore      thrpt    5  110,877 ±  0,606  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:get  thrpt    5   55,447 ±  0,244  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:put  thrpt    5   55,430 ±  0,368  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore                        thrpt    5   75,079 ± 24,611  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:get                    thrpt    5   37,540 ± 12,305  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:put                    thrpt    5   37,540 ± 12,306  ops/ns

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

    Benchmark Zing Falcon                                                                                       Mode  Cnt    Score   Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt    5  122.427 ± 6.056  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt    5   61.213 ± 3.029  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt    5   61.214 ± 3.027  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore      thrpt    5  116.057 ± 2.700  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:get  thrpt    5   58.031 ± 1.343  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:put  thrpt    5   58.027 ± 1.357  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore                        thrpt    5  100.638 ± 5.084  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:get                    thrpt    5   50.330 ± 2.570  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:put                    thrpt    5   50.308 ± 2.517  ops/ns

    Benchmark Oracle jdk 11.0.2                                                                                Mode  Cnt    Score   Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt   40  114.136 ± 1.590  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt   40   57.067 ± 0.795  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt   40   57.069 ± 0.795  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore      thrpt   40  103.765 ± 1.448  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:get  thrpt   40   51.885 ± 0.723  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:put  thrpt   40   51.880 ± 0.725  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore                        thrpt   40   98.693 ± 0.386  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:get                    thrpt   40   49.348 ± 0.194  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:put                    thrpt   40   49.345 ± 0.194  ops/ns

    Test 2
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 4;
    private static final int getThreads = 4;
    thread count = 8

    ArrayBlockingQueue and CyclicLockOnEntryStore have near the same throughput,
    Take a look that Zing C2 is old JIT compiler and not updated. Azul recomends use Falcon instead.

    Benchmark Oracle 9 Windows                                                                                  Mode  Cnt    Score    Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt    5  106,075 ±  6,182  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt    5   52,671 ±  2,568  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt    5   53,404 ±  3,947  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore      thrpt    5  112,511 ±  2,337  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:get  thrpt    5   56,342 ±  1,472  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:put  thrpt    5   56,169 ±  0,939  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore                        thrpt    5   95,681 ± 12,696  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:get                    thrpt    5   47,834 ±  6,413  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:put                    thrpt    5   47,847 ±  6,284  ops/ns

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

    Benchmark                                                                                                   Mode  Cnt    Score    Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt    5   60.738 ± 15.750  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt    5   32.089 ±  4.245  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt    5   28.649 ± 13.565  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore      thrpt    5  116.789 ±  3.617  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:get  thrpt    5   58.416 ±  1.869  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:put  thrpt    5   58.372 ±  1.752  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore                        thrpt    5  101.789 ±  8.372  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:get                    thrpt    5   50.892 ±  4.181  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:put                    thrpt    5   50.898 ±  4.191  ops/ns

    Benchmark                                                                                                   Mode  Cnt    Score   Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt   40   60.781 ± 1.750  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt   40   30.406 ± 0.877  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt   40   30.375 ± 0.886  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore      thrpt   40   99.789 ± 0.455  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:get  thrpt   40   49.900 ± 0.229  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:put  thrpt   40   49.889 ± 0.227  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore                        thrpt   40  109.422 ± 0.926  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:get                    thrpt   40   54.710 ± 0.467  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:put                    thrpt   40   54.712 ± 0.461  ops/ns

    Test 3
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 8;
    private static final int getThreads = 8;
    thread count = 16

    Now ArrayBlockingQueue gives the worst performance

    Benchmark Oracle 9 Windows                                                                                                      Mode  Cnt    Score    Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt    5   54,929 ± 19,616  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt    5   27,307 ± 12,961  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt    5   27,621 ± 14,321  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore      thrpt    5  108,046 ±  2,329  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:get  thrpt    5   54,023 ±  1,079  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:put  thrpt    5   54,023 ±  1,324  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore                        thrpt    5   95,156 ± 26,601  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:get                    thrpt    5   47,581 ± 13,307  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:put                    thrpt    5   47,576 ± 13,294  ops/ns

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

    Benchmark                                                                                                   Mode  Cnt    Score   Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt   40   35.561 ± 3.231   ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt   40   17.840 ± 1.843   ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt   40   17.721 ± 1.938   ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore      thrpt   40  116.401 ± 2.940   ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:get  thrpt   40   58.161 ± 1.551   ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:put  thrpt   40   58.240 ± 1.408   ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore                        thrpt   40  132.267 ± 14.966  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:get                    thrpt   40   66.080 ± 29.500  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:put                    thrpt   40   66.105 ± 29.306  ops/ns

    Benchmark     Oracle jdk 11.0.2                                                                             Mode  Cnt    Score   Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt   40   29.354 ± 1.285  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt   40   14.716 ± 0.672  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt   40   14.638 ± 0.744  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore      thrpt   40   99.885 ± 0.502  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:get  thrpt   40   49.939 ± 0.252  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:put  thrpt   40   49.946 ± 0.253  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore                        thrpt   40  130.652 ± 1.595  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:get                    thrpt   40   65.321 ± 0.797  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:put                    thrpt   40   65.332 ± 0.798  ops/ns

    Test 4
    Now we try single producer multiple reader strategy which is used in many paradigm of programming(For ex see Disruptor)

    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 1;
    private static final int getThreads = 15;
    thread count = 16

    Benchmark Oracle 9 Windows                                                                                  Mode  Cnt   Score    Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt    5   47,578 ±  3,669  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt    5   23,797 ±  1,825  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt    5   23,780 ±  1,844  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore      thrpt    5   92,511 ± 14,435  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:get  thrpt    5   45,954 ±  7,806  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:put  thrpt    5   46,556 ±  6,648  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore                        thrpt    5  101,095 ± 37,601  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:get                    thrpt    5   50,548 ± 18,801  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:put                    thrpt    5   50,547 ± 18,800  ops/ns

    Benchmark Oracle 10                                                                                         Mode  Cnt    Score    Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt    5   22.615 ±  4.910  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt    5   11.315 ±  2.472  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt    5   11.300 ±  2.439  ops/ns
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

    Benchmark                                                                                                   Mode  Cnt    Score   Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt   40   14.553 ± 0.405  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt   40    7.236 ± 0.216  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt   40    7.317 ± 0.196  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore      thrpt   40  111.650 ± 0.431  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:get  thrpt   40   55.775 ± 0.238  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:put  thrpt   40   55.875 ± 0.198  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore                        thrpt   40   44.720 ± 6.703  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:get                    thrpt   40   22.496 ± 3.336  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:put                    thrpt   40   22.224 ± 3.370  ops/ns

    Benchmark Oracle                                                                                            Mode  Cnt    Score   Error   Units
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue              thrpt   40   21.679 ± 0.432  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:get          thrpt   40   10.843 ± 0.218  ops/ns
    producerConsumer.FIFO.FIFOThroughput.ArrayBlockingQueueBenchmarkManyPutGet.ArrayBlockingQueue:put          thrpt   40   10.836 ± 0.215  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore      thrpt   40  105.538 ± 1.215  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:get  thrpt   40   52.907 ± 0.619  ops/ns
    producerConsumer.FIFO.FIFOThroughput.CyclicLockOnEntryStoreBenchmarkManyPutGet.CyclicLockOnEntryStore:put  thrpt   40   52.630 ± 0.600  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore                        thrpt   40   56.490 ± 8.217  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:get                    thrpt   40   28.429 ± 4.052  ops/ns
    producerConsumer.FIFO.FIFOThroughput.TwoLocksStoreBenchmarkManyPutGet.TwoLocksStore:put                    thrpt   40   28.061 ± 4.182  ops/ns
 */

public class FIFOThroughput {

    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 4;
    private static final int getThreads = 4;
    private static final int threadsCount = getThreads + putThreads;

    @State(Scope.Thread)
    public static class Tested {
        public byte data[] = new byte[1000];
    }

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

        private ArrayBlockingQueue<Tested> arrayBlockingQueue;

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
            arrayBlockingQueue.put(new Tested());
        }

        @Benchmark
        @Group("ArrayBlockingQueue")
        @GroupThreads(getThreads)
        public Tested get() throws InterruptedException {
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

        private IStore<Tested> simple;
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
            simple.put(new Tested());
        }

        @Benchmark
        @Group("CyclicLockOnEntryStore")
        @GroupThreads(getThreads)
        public Tested get() throws InterruptedException {
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

        private IStore<Tested> simple;
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
            simple.put(new Tested());
        }

        @Benchmark
        @Group("TwoLocksStore")
        @GroupThreads(getThreads)
        public Tested get() throws InterruptedException {
            return simple.get();
        }
    }

    public static void main(String[] args) {
        Options opt = new OptionsBuilder()
                .include(FIFOThroughput.class.getSimpleName())
                .warmupIterations(30)
                .measurementIterations(20)
               // .operationsPerInvocation(insert_value)
                .forks(1)
                .threads(threadsCount)
                .timeout(TimeValue.seconds(6))
                // .shouldDoGC(true)
                .syncIterations(true)
                //  .jvmArgs("-XX:+UseFalcon","-XX:+LogVMOutput","-XX:+PrintCompilation","-XX:+TraceDeoptimization","-XX:LogFile=/home/ek/zing-jvm2.log")
                //.jvmArgs("-XX:+UseFalcon", "-XX:+UnlockDiagnosticVMOptions", "-XX:CompileCommand=print, producerConsumer.FIFO.TwoLocksStore::*")
                //    .jvmArgs("-XX:+UseFalcon")
                .jvmArgs("-XX:+ParallelGC")
                .jvmArgs("-server","-Xms1024m", "-Xmx1024m","-XX:+UseLargePages", "-XX:LargePageSizeInBytes=2m")
                .build();
        try {
            new Runner(opt).run();
        } catch (RunnerException e) {
            e.printStackTrace();
        }
    }
}