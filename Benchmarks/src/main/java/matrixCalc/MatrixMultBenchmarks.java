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
    Benchmark                                               Mode  Cnt      Score     Error  Units
    matrixCalc.MatrixMultBenchmarks.cacheLineBound1024      avgt    5    909.245 ±   8.681  ms/op
    matrixCalc.MatrixMultBenchmarks.cacheLineBound512       avgt    5   1034.488 ±   4.065  ms/op
    matrixCalc.MatrixMultBenchmarks.cacheLineBound256       avgt    5    935.842 ±  10.153  ms/op
    matrixCalc.MatrixMultBenchmarks.cacheLineBound128       avgt    5    924.581 ±   8.953  ms/op
    matrixCalc.MatrixMultBenchmarks.cacheLineBound64        avgt    5   1203.797 ±  15.822  ms/op

    matrixCalc.MatrixMultBenchmarks.simple                  avgt    5  14781.306 ±  37.917  ms/op

    matrixCalc.MatrixMultBenchmarks.transpose               avgt    5    851.304 ±  26.713  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine1024  avgt    5    947.147 ±   6.110  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine512   avgt    5   1085.929 ±  11.475  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine256   avgt    5    941.664 ±  22.719  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine128   avgt    5   1044.785 ±  13.920  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine64    avgt    5   1245.828 ± 173.741  ms/op

    Zing -XX:+UseFalcon

    Benchmark                                               Mode  Cnt     Score    Error  Units
    matrixCalc.MatrixMultBenchmarks.cacheLineBound1024      avgt    5  1215.127 ± 59.797  ms/op
    matrixCalc.MatrixMultBenchmarks.cacheLineBound512       avgt    5  1340.103 ± 16.530  ms/op
    matrixCalc.MatrixMultBenchmarks.cacheLineBound256       avgt    5  1479.840 ±  5.721  ms/op
    matrixCalc.MatrixMultBenchmarks.cacheLineBound128       avgt    5  1823.015 ± 20.820  ms/op
    matrixCalc.MatrixMultBenchmarks.cacheLineBound64        avgt    5  1640.370 ±  4.591  ms/op

    matrixCalc.MatrixMultBenchmarks.simple                  avgt    5  4601.778 ±  22.004  ms/op

    matrixCalc.MatrixMultBenchmarks.transpose               avgt    5   776.250 ±   8.961  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine1024  avgt    5  1214.823 ±  11.461  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine512   avgt    5  1337.816 ±  10.505  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine256   avgt    5  1474.943 ±   5.211  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine128   avgt    5  1844.958 ±  27.301  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine64    avgt    5  2469.070 ±  12.742  ms/op

    Zing -XX:+UseC2

    Benchmark                                               Mode  Cnt     Score     Error  Units
    matrixCalc.MatrixMultBenchmarks.cacheLineBound1024      avgt    5   732.345 ±   8.921  ms/op
    matrixCalc.MatrixMultBenchmarks.cacheLineBound512       avgt    5   886.812 ±   6.705  ms/op
    matrixCalc.MatrixMultBenchmarks.cacheLineBound256       avgt    5  1072.489 ±  29.699  ms/op
    matrixCalc.MatrixMultBenchmarks.cacheLineBound128       avgt    5  1410.441 ±  43.067  ms/op
    matrixCalc.MatrixMultBenchmarks.cacheLineBound64        avgt    5  2049.377 ±   5.894  ms/op

    matrixCalc.MatrixMultBenchmarks.simple                  avgt    5  4728.626 ± 138.376  ms/op

    matrixCalc.MatrixMultBenchmarks.transpose               avgt    5   811.420 ±  10.334  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine1024  avgt    5   753.779 ±  19.075  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine512   avgt    5   881.844 ±  26.300  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine256   avgt    5  1051.126 ±  32.413  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine128   avgt    5  1368.654 ±   8.337  ms/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine64    avgt    5  1949.641 ±  13.534  ms/op
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@Fork(1)
public class MatrixMultBenchmarks {

    private long[][] p1, p2;

    @Setup
    public void setup() {
        p1 = new long[1024][1024];
        p2 = new long[1024][1024];

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
    public long[][] simple() {
        MatrixCalc matrixCalc = new Simple();
        return matrixCalc.multiply(p1, p2);
    }

    @Benchmark
    public long[][] transpose() {

        Transpose matrixCalc = new Transpose();
        return matrixCalc.calcResult(p1, p2);
    }

    @Benchmark
    public long[][] transposeCacheLine64() {
        MatrixCalc matrixCalc = new TransposeCacheLine(64);
        return matrixCalc.multiply(p1, p2);
    }

    @Benchmark
    public long[][] transposeCacheLine128() {
        MatrixCalc matrixCalc = new TransposeCacheLine(128);
        return matrixCalc.multiply(p1, p2);
    }

    @Benchmark
    public long[][] transposeCacheLine256() {
        MatrixCalc matrixCalc = new TransposeCacheLine(256);
        return matrixCalc.multiply(p1, p2);
    }

    @Benchmark
    public long[][] transposeCacheLine512() {
        MatrixCalc matrixCalc = new TransposeCacheLine(512);
        return matrixCalc.multiply(p1, p2);
    }

    @Benchmark
    public long[][] transposeCacheLine1024() {
        MatrixCalc matrixCalc = new TransposeCacheLine(1024);
        return matrixCalc.multiply(p1, p2);
    }

    @Benchmark
    public long[][] cacheLineBound64() {
        MatrixCalc matrixCalc = new CacheLineBound(64);
        return matrixCalc.multiply(p1, p2);
    }

    @Benchmark
    public long[][] cacheLineBound128() {
        MatrixCalc matrixCalc = new CacheLineBound(128);
        return matrixCalc.multiply(p1, p2);
    }

    @Benchmark
    public long[][] cacheLineBound256() {
        MatrixCalc matrixCalc = new CacheLineBound(256);
        return matrixCalc.multiply(p1, p2);
    }

    @Benchmark
    public long[][] cacheLineBound512() {
        MatrixCalc matrixCalc = new CacheLineBound(512);
        return matrixCalc.multiply(p1, p2);
    }

    @Benchmark
    public long[][] cacheLineBound1024() {
        MatrixCalc matrixCalc = new CacheLineBound(1024);
        return matrixCalc.multiply(p1, p2);
    }

    public static void main(String[] args) {

        Options opt = new OptionsBuilder()

                .include(MatrixMultBenchmarks.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                //.shouldDoGC(true)
                //.jvmArgs("-XX:+UseC2")
                //.jvmArgs("-XX:+UseFalcon")
                .build();
        try {
            new Runner(opt).run();
        } catch (RunnerException e) {
            e.printStackTrace();
        }
    }
}
