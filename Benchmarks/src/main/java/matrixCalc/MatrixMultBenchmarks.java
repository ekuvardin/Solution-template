package matrixCalc;

import generator.Generator;
import matrixCalc.impl.CacheLineBound;
import matrixCalc.impl.Simple;
import matrixCalc.impl.Transpose;
import matrixCalc.impl.TransposeCacheLine;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/*
    Benchmarks was running using Intel Core i502310 CPU 2.90GHZ 3.20 GHZ 4 cores Ubuntu 17.0

    simple - realization using simple 3 cycles
    transpose - first transpose matrix and then multiply
    cacheLineBoundXXX - simple realization but additional 3 cycles each with step == XXX/8

    Using <simple> algorithm we jump from one row to another look
    for(int k=0;k<n;k++)
     c[i][j] = a[j][k]*b[k][j];

    Increasing k we get values from a in consistent manner(+1 shift), but from array c we every time do +n shift

    Using transport matrix we fetch records in consistent order in both a,b and processors can access array in consistent manner
    for(int k=0;k<n;k++)
     c[i][j] = a[j][k]*b[j][k];

    In <cacheLineBound64> we split arrays so that we take values to fulfill one cache lines (64)/8 = 16 values
    How it can be faster than simple in order to that we have 3 additional loops?

    Just write several iterate with simple algorithm
    c[0][0] = a[0][1..N]* b[1..N][0]
    c[0][1] = a[0][1..N]* b[1..N][1]
    We use the same value a[0][1..N] N times
    And processor and tries to cache our value but we have a lot of values and some of them washed out
    To washed out we can reduce step so that a[0][1..K] can hold cache line

    In <transposeCacheLineXXX> I try to compose transpose + CacheLine method XXX - means XXX/8 values in one step
    We see as increasing cache we get result closer to transpose.

    Oracle jdk 10.0
    Benchmark                                               Mode  Cnt      Score    Error  Units
    matrixCalc.MatrixMultBenchmarks.cacheLineBound1024      avgt    5   1826.487 ± 55.554  ms/op
    matrixCalc.MatrixMultBenchmarks.cacheLineBound128       avgt    5   1898.004 ± 31.179  ms/op
    matrixCalc.MatrixMultBenchmarks.cacheLineBound256       avgt    5   1908.504 ± 16.251  ms/op
    matrixCalc.MatrixMultBenchmarks.cacheLineBound512       avgt    5   2044.415 ± 15.045  ms/op
    matrixCalc.MatrixMultBenchmarks.cacheLineBound64        avgt    5   2482.501 ± 18.270  ms/op

    matrixCalc.MatrixMultBenchmarks.simple                  avgt    5  15984.091 ± 37.772  ms/op

    matrixCalc.MatrixMultBenchmarks.transpose               avgt    5    978.870 ± 25.068  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine128   avgt    5   1133.818 ± 17.162  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine1280  avgt    5    908.534 ± 17.958  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine256   avgt    5   1043.841 ± 14.231  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine512   avgt    5   1053.568 ± 22.450  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine64    avgt    5   1318.300 ± 22.128  ms/op

    Zing Falcon JIT

    Benchmark                                               Mode  Cnt     Score    Error  Units
    matrixCalc.MatrixMultBenchmarks.cacheLineBound1024      avgt    5  2055.222 ±  6.253  ms/op
    matrixCalc.MatrixMultBenchmarks.cacheLineBound128       avgt    5  2442.114 ± 87.660  ms/op
    matrixCalc.MatrixMultBenchmarks.cacheLineBound256       avgt    5  2306.102 ±  8.209  ms/op
    matrixCalc.MatrixMultBenchmarks.cacheLineBound512       avgt    5  2213.158 ±  4.102  ms/op
    matrixCalc.MatrixMultBenchmarks.cacheLineBound64        avgt    5  2836.654 ±  3.120  ms/op

    matrixCalc.MatrixMultBenchmarks.simple                  avgt    5  7298.752 ± 93.274  ms/op

    matrixCalc.MatrixMultBenchmarks.transpose               avgt    5  1626.396 ± 11.066  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine128   avgt    5  2220.941 ±  7.191  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine1280  avgt    5  2039.196 ± 21.224  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine256   avgt    5  2336.079 ± 12.301  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine512   avgt    5  2168.803 ± 80.669  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine64    avgt    5  2462.167 ± 10.506  ms/op

    Zing C2 JIT

    Benchmark                                               Mode  Cnt     Score    Error  Units
    matrixCalc.MatrixMultBenchmarks.cacheLineBound1024      avgt    5  1862.110 ± 24.058  ms/op
    matrixCalc.MatrixMultBenchmarks.cacheLineBound128       avgt    5  1879.257 ± 16.557  ms/op
    matrixCalc.MatrixMultBenchmarks.cacheLineBound256       avgt    5  1707.919 ± 15.722  ms/op
    matrixCalc.MatrixMultBenchmarks.cacheLineBound512       avgt    5  1648.103 ±  9.675  ms/op
    matrixCalc.MatrixMultBenchmarks.cacheLineBound64        avgt    5  2615.763 ± 10.417  ms/op

    matrixCalc.MatrixMultBenchmarks.simple                  avgt    5  4229.462 ± 95.308  ms/op

    matrixCalc.MatrixMultBenchmarks.transpose               avgt    5   949.843 ± 23.242  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine128   avgt    5  1410.886 ±  3.688  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine1280  avgt    5   866.658 ± 15.250  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine256   avgt    5  1102.054 ±  7.427  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine512   avgt    5   978.760 ± 23.288  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine64    avgt    5  1928.003 ± 18.054  ms/op
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@Fork(1)
public class MatrixMultBenchmarks {

    long[][] p1, p2, p3;

    @Setup
    public void setup() {
        p1 = new long[1024][1024];
        p2 = new long[1024][1024];
        p3 = new long[1024][1024];

        try {
            Generator.main("-res Out.txt".split(" "));
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new RuntimeException("Error while open scanner");
        }

        try (Scanner scanner = new Scanner(new File("Out.txt"), StandardCharsets.UTF_8.toString())) {
            for (int i = 0; i < p1.length; i++) {
                for (int j = 0; j < p1[0].length && scanner.hasNextLong(); j++) {
                    p2[i][j] = p1[i][j] = scanner.nextLong();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while open scanner");
        }
    }

    @Benchmark
    public void simple() {
        MatrixCalc matrixCalc = new Simple();
        matrixCalc.multiply(p1, p2, p3);
    }

    @Benchmark
    public void transpose() {
        MatrixCalc matrixCalc = new Transpose();
        matrixCalc.multiply(p1, p2, p3);
    }

    @Benchmark
    public void transposeCacheLine64() {
        MatrixCalc matrixCalc = new TransposeCacheLine(64);
        matrixCalc.multiply(p1, p2, p3);
    }

    @Benchmark
    public void transposeCacheLine128() {
        MatrixCalc matrixCalc = new TransposeCacheLine(128);
        matrixCalc.multiply(p1, p2, p3);
    }

    @Benchmark
    public void transposeCacheLine256() {
        MatrixCalc matrixCalc = new TransposeCacheLine(256);
        matrixCalc.multiply(p1, p2, p3);
    }

    @Benchmark
    public void transposeCacheLine512() {
        MatrixCalc matrixCalc = new TransposeCacheLine(512);
        matrixCalc.multiply(p1, p2, p3);
    }

    @Benchmark
    public void transposeCacheLine1280() {
        MatrixCalc matrixCalc = new TransposeCacheLine(1024);
        matrixCalc.multiply(p1, p2, p3);
    }

    @Benchmark
    public void cacheLineBound64() {
        MatrixCalc matrixCalc = new CacheLineBound(64);
        matrixCalc.multiply(p1, p2, p3);
    }

    @Benchmark
    public void cacheLineBound128() {
        MatrixCalc matrixCalc = new CacheLineBound(128);
        matrixCalc.multiply(p1, p2, p3);
    }

    @Benchmark
    public void cacheLineBound256() {
        MatrixCalc matrixCalc = new CacheLineBound(256);
        matrixCalc.multiply(p1, p2, p3);
    }

    @Benchmark
    public void cacheLineBound512() {
        MatrixCalc matrixCalc = new CacheLineBound(512);
        matrixCalc.multiply(p1, p2, p3);
    }

    @Benchmark
    public void cacheLineBound1024() {
        MatrixCalc matrixCalc = new CacheLineBound(1024);
        matrixCalc.multiply(p1, p2, p3);
    }

    public static void main(String[] args) {

        Options opt = new OptionsBuilder()
                .include(MatrixMultBenchmarks.class.getSimpleName())
                .warmupIterations(4)
                .measurementIterations(5)
                .forks(1)
                .jvmArgs("-XX:+UseC2")
                .build();
        try {
            new Runner(opt).run();
        } catch (RunnerException e) {
            e.printStackTrace();
        }
    }
}
