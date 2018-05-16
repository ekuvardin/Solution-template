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

    Oracle java 8 Windows 7
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

    Oracle java 10 Ubuntu 17.0
    Benchmark                                                                                             Mode  Cnt    Score    Error   Units
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque      thrpt    5  105.232 ±  6.069  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:get  thrpt    5   52.617 ±  3.033  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:put  thrpt    5   52.615 ±  3.036  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store                                  thrpt    5  117.840 ± 12.353  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:get                              thrpt    5   58.912 ±  6.135  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:put                              thrpt    5   58.928 ±  6.231  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark                  thrpt    5  123.732 ± 16.608  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:get              thrpt    5   61.859 ±  8.311  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:put              thrpt    5   61.873 ±  8.297  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore                      thrpt    5  141.066 ± 21.897  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:get                  thrpt    5   70.533 ± 10.951  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:put                  thrpt    5   70.533 ± 10.945  ops/ns

    Zing Falcon Ubuntu 17.0
    Benchmark                                                                                             Mode  Cnt    Score    Error   Units
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque      thrpt    5  135.468 ±  1.785  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:get  thrpt    5   67.732 ±  0.897  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:put  thrpt    5   67.736 ±  0.888  ops/ns

    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store                                  thrpt    5  126.580 ±  4.946  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:get                              thrpt    5   63.256 ±  2.393  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:put                              thrpt    5   63.323 ±  2.565  ops/ns

    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark                  thrpt    5  119.760 ± 30.014  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:get              thrpt    5   59.880 ± 15.006  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:put              thrpt    5   59.880 ± 15.008  ops/ns

    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore                      thrpt    5  140.193 ± 30.161  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:get                  thrpt    5   70.097 ± 15.076  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:put                  thrpt    5   70.096 ± 15.085  ops/ns

    Zing C2 Ubuntu 17.0
    Benchmark                                                                                             Mode  Cnt    Score    Error   Units
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque      thrpt    5  116.992 ±  3.440  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:get  thrpt    5   58.497 ±  1.718  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:put  thrpt    5   58.494 ±  1.722  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store                                  thrpt    5   93.262 ±  5.612  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:get                              thrpt    5   46.618 ±  2.739  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:put                              thrpt    5   46.644 ±  2.879  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark                  thrpt    5   90.805 ± 12.692  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:get              thrpt    5   45.402 ±  6.345  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:put              thrpt    5   45.402 ±  6.348  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore                      thrpt    5  134.918 ± 48.819  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:get                  thrpt    5   67.459 ± 24.410  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:put                  thrpt    5   67.459 ± 24.409  ops/ns

    Test 2
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 4;
    private static final int getThreads = 4;
    thread count = 8

    Oracle java 8 Windows 7
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

    Oracle java 10 Ubuntu 17.0
    Benchmark                                                                                             Mode  Cnt    Score    Error   Units
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque      thrpt    5   56.414 ±  3.485  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:get  thrpt    5   28.429 ±  2.754  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:put  thrpt    5   27.984 ±  2.460  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store                                  thrpt    5  113.122 ± 12.998  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:get                              thrpt    5   56.593 ±  7.671  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:put                              thrpt    5   56.529 ±  5.580  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark                  thrpt    5  106.907 ±  6.445  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:get              thrpt    5   53.452 ±  3.220  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:put              thrpt    5   53.454 ±  3.226  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore                      thrpt    5  136.963 ± 23.406  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:get                  thrpt    5   68.485 ± 11.697  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:put                  thrpt    5   68.478 ± 11.709  ops/ns

    Zing Falcon Ubuntu 17.0
    Benchmark                                                                                             Mode  Cnt    Score    Error   Units
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque      thrpt    5   57.917 ±  7.315  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:get  thrpt    5   28.391 ±  6.236  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:put  thrpt    5   29.526 ±  2.679  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store                                  thrpt    5  117.309 ± 47.354  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:get                              thrpt    5   58.574 ± 23.954  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:put                              thrpt    5   58.735 ± 23.402  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark                  thrpt    5  105.723 ±  6.139  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:get              thrpt    5   52.861 ±  3.073  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:put              thrpt    5   52.862 ±  3.066  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore                      thrpt    5  128.260 ± 22.442  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:get                  thrpt    5   64.129 ± 11.216  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:put                  thrpt    5   64.130 ± 11.225  ops/ns

    Zing C2 Ubuntu 17.0
    Benchmark                                                                                             Mode  Cnt    Score    Error   Units
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque      thrpt    5   56.185 ±  3.675  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:get  thrpt    5   28.059 ±  1.338  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:put  thrpt    5   28.127 ±  2.406  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store                                  thrpt    5  105.448 ± 21.754  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:get                              thrpt    5   52.748 ± 11.248  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:put                              thrpt    5   52.700 ± 10.508  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark                  thrpt    5   85.985 ±  6.911  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:get              thrpt    5   42.993 ±  3.456  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:put              thrpt    5   42.992 ±  3.455  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore                      thrpt    5  124.138 ± 14.247  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:get                  thrpt    5   62.073 ±  7.145  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:put                  thrpt    5   62.065 ±  7.102  ops/ns


    Test 3
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 8;
    private static final int getThreads = 8;
    thread count = 16

    Oracle java 8 Windows 7
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

    Oracle java 10 Ubuntu 17.0
    Benchmark                                                                                             Mode  Cnt    Score    Error   Units
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque      thrpt    5   27.582 ±  2.866  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:get  thrpt    5   13.160 ±  2.332  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:put  thrpt    5   14.422 ±  2.381  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store                                  thrpt    5   86.157 ± 44.207  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:get                              thrpt    5   43.033 ± 21.958  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:put                              thrpt    5   43.124 ± 22.369  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark                  thrpt    5   98.220 ±  4.030  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:get              thrpt    5   49.110 ±  2.020  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:put              thrpt    5   49.111 ±  2.010  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore                      thrpt    5  129.324 ± 17.712  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:get                  thrpt    5   64.662 ±  8.857  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:put                  thrpt    5   64.662 ±  8.855  ops/ns

    Zing Falcon Ubuntu 17.0
    Benchmark                                                                                             Mode  Cnt    Score    Error   Units
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque      thrpt    5   34.808 ± 36.640  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:get  thrpt    5   14.802 ± 10.983  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:put  thrpt    5   20.006 ± 36.468  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store                                  thrpt    5   98.907 ± 11.788  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:get                              thrpt    5   49.383 ±  5.919  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:put                              thrpt    5   49.525 ±  5.955  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark                  thrpt    5  102.160 ± 12.098  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:get              thrpt    5   51.080 ±  6.048  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:put              thrpt    5   51.080 ±  6.050  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore                      thrpt    5  114.174 ±  5.336  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:get                  thrpt    5   57.086 ±  2.664  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:put                  thrpt    5   57.088 ±  2.673  ops/ns

    Zing C2 Ubuntu 17.0
    Benchmark                                                                                             Mode  Cnt    Score    Error   Units
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque      thrpt    5   45.218 ± 60.179  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:get  thrpt    5   26.223 ± 60.298  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:put  thrpt    5   18.995 ± 22.953  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store                                  thrpt    5   86.931 ± 15.596  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:get                              thrpt    5   43.503 ±  7.018  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:put                              thrpt    5   43.429 ±  8.733  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark                  thrpt    5   94.010 ±  1.849  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:get              thrpt    5   47.005 ±  0.919  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:put              thrpt    5   47.005 ±  0.930  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore                      thrpt    5  101.707 ± 27.547  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:get                  thrpt    5   50.846 ± 13.803  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:put                  thrpt    5   50.861 ± 13.745  ops/ns

    Test 4(Now we try single producer multiple consumer strategy)
    private static final int size = 128;
    private static final int insert_value = 10000;
    private static final int putThreads = 1;
    private static final int getThreads = 15;
    thread count = 16

    Oracle java 8 Windows 7
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

    Oracle java 10 Ubuntu 17.0
    Benchmark                                                                                             Mode  Cnt   Score    Error   Units
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque      thrpt    5  17.523 ±  9.577  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:get  thrpt    5   8.761 ±  4.823  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:put  thrpt    5   8.763 ±  4.754  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store                                  thrpt    5  28.565 ± 53.745  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:get                              thrpt    5  14.312 ± 25.678  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:put                              thrpt    5  14.254 ± 28.068  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark                  thrpt    5  39.423 ±  2.843  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:get              thrpt    5  19.709 ±  1.406  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:put              thrpt    5  19.714 ±  1.437  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore                      thrpt    5  42.737 ± 38.075  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:get                  thrpt    5  21.394 ± 19.032  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:put                  thrpt    5  21.342 ± 19.043  ops/ns

    Zing Falcon Ubuntu 17.0
    Benchmark                                                                                             Mode  Cnt   Score    Error   Units
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque      thrpt    5  14.525 ±  3.131  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:get  thrpt    5   7.293 ±  1.503  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:put  thrpt    5   7.232 ±  1.663  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store                                  thrpt    5  59.922 ± 42.331  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:get                              thrpt    5  30.180 ± 21.998  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:put                              thrpt    5  29.741 ± 20.334  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark                  thrpt    5  13.238 ±  0.247  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:get              thrpt    5   6.618 ±  0.127  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:put              thrpt    5   6.620 ±  0.120  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore                      thrpt    5  82.518 ± 27.329  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:get                  thrpt    5  41.283 ± 13.825  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:put                  thrpt    5  41.235 ± 13.504  ops/ns

    Zing C2 Ubuntu 17.0
    Benchmark                                                                                             Mode  Cnt   Score    Error   Units
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque      thrpt    5  13.648 ±  2.244  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:get  thrpt    5   6.810 ±  1.126  ops/ns
    producerConsumer.LIFO.LIFOThroughput.LinkedBlockingDequeBenchmarkManyPutGet.LinkedBlockingDeque:put  thrpt    5   6.838 ±  1.121  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store                                  thrpt    5  56.728 ± 23.260  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:get                              thrpt    5  28.559 ± 12.197  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreBenchmarkManyPutGet.Store:put                              thrpt    5  28.169 ± 11.113  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark                  thrpt    5  11.701 ±  8.082  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:get              thrpt    5   5.850 ±  4.040  ops/ns
    producerConsumer.LIFO.LIFOThroughput.StoreWithParkBenchmarkManyPutGet.StoreWithPark:put              thrpt    5   5.851 ±  4.042  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore                      thrpt    5  75.026 ± 37.527  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:get                  thrpt    5  37.545 ± 18.780  ops/ns
    producerConsumer.LIFO.LIFOThroughput.TrickyStoreBenchmarkManyPutGet.TrickyStore:put                  thrpt    5  37.480 ± 18.748  ops/ns

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
                .warmupIterations(20)
                .measurementIterations(5)
                .operationsPerInvocation(insert_value)
                .forks(1)
                .threads(threadsCount)
                .timeout(TimeValue.seconds(5))
                .syncIterations(true)
                .jvmArgs("-XX:+UseParallelGC")
              //  .jvmArgs("-XX:+UseFalcon")
               // .jvmArgs("-XX:+UseC2")
               // .shouldDoGC(true)
                .build();
        try {
            new Runner(opt).run();
        } catch (RunnerException e) {
            e.printStackTrace();
        }
    }
}
