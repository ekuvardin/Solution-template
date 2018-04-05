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
    MatrixMultBenchmarks results on x86 4 cores

    simple - realization using simple 3 cycles
    transpose - first transpose matrix and then multiply
    cacheLineBoundXXX - simple realization but additional 3 cycles each with step == XXX/8

    Benchmark                                               Mode  Cnt            Score           Error  Units
    matrixCalc.MatrixMultBenchmarks.simple                  avgt    5  16264646964,200 ± 144770891,123  ns/op
    matrixCalc.MatrixMultBenchmarks.cacheLineBound64        avgt    5   1606312512,800 ±   7409967,584  ns/op
    matrixCalc.MatrixMultBenchmarks.transpose               avgt    5    828582775,700 ±  27506792,346  ns/op

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

    Benchmark                                               Mode  Cnt           Score           Error  Units
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine64    avgt    5  1224212651,800 ±  21587743,317  ns/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine128   avgt    5  1083596195,600 ±  45830833,954  ns/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine256   avgt    5  1062897902,400 ± 520888805,480  ns/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine512   avgt    5   999335083,900 ±  42770130,990  ns/op
    matrixCalc.MatrixMultBenchmarks.transposeCacheLine1024  avgt    5   922527225,000 ±  28701337,357  ns/op
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@Fork(1)
public class MatrixMultBenchmarks {

    long[][] p1, p2, p3;

    @Setup
    public void setup() {
        p1 = new long[2048][2048];
        p2 = new long[2048][2048];
        p3 = new long[2048][2048];

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
    public void cacheLineBound128() {
        MatrixCalc matrixCalc = new CacheLineBound(128);
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
        MatrixCalc matrixCalc = new TransposeCacheLine(1280);
        matrixCalc.multiply(p1, p2, p3);
    }

    @Benchmark
    public void cacheLineBound() {
        MatrixCalc matrixCalc = new TransposeCacheLine(128);
        matrixCalc.multiply(p1, p2, p3);
    }

    public static void main(String[] args) {

        Options opt = new OptionsBuilder()
                .include(MatrixMultBenchmarks.class.getSimpleName())
                .warmupIterations(2)
                .measurementIterations(5)
                .forks(1)
                .jvmArgs("-ea")
                .build();
        try {
            new Runner(opt).run();
        } catch (RunnerException e) {
            e.printStackTrace();
        }
    }
}
