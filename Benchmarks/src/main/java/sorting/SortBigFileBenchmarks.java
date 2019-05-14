package sorting;

import generator.Generator;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import sortBigFile.readers.integerReader.IntegerCompareStrategy;
import sortBigFile.sort.SortBigFile;
import sortBigFile.writers.integerWriters.IntegerScanner;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

/*
  Benchmarks on x86 processors 8 cores

  MaxChunkLen = 32768
  MaxCountOfChunks = 40
  Created 61 files
  Input file size ~ 15mb

  Benchmark                                   Mode  Cnt           Score           Error  Units
  sorting.SortBigFileBenchmarks.sort          avgt    5  7048881910,000 ± 265928097,403  ns/op
  sorting.SortBigFileBenchmarks.sortParallel  avgt    5  5208340985,800 ± 372153984,334  ns/op

  MaxChunkLen = 16384
  MaxCountOfChunks = 40
  Created 122 files

  Benchmark                                   Mode  Cnt           Score           Error  Units
  sorting.SortBigFileBenchmarks.sort          avgt    5  8333430252,200 ± 428178438,427  ns/op
  sorting.SortBigFileBenchmarks.sortParallel  avgt    5  5893749102,200 ± 472300195,774  ns/op

  MaxChunkLen = 8192
  MaxCountOfChunks = 40
  Created 244 files

  Benchmark                                   Mode  Cnt           Score           Error  Units
  sorting.SortBigFileBenchmarks.sort          avgt    5  8363970167,600 ± 367984508,016  ns/op
  sorting.SortBigFileBenchmarks.sortParallel  avgt    5  5909683122,200 ± 442011119,673  ns/op

  We see that decreasing total amount of array we get more acceleration but at the end we stuck in some threshold.

    When we increasing file size acceleration increasing dramatically. Look at Benchmarks below
    MaxChunkLen = 8192
    MaxCountOfChunks = 40
    Created 163 files
    Input file size ~ 50mb
    Number of threads in parallel:4

    Oracle 11
    Benchmark                           Mode  Cnt   Score   Error  Units
    SortBigFileBenchmarks.sort          avgt    5  41.360 ± 5.742   s/op
    SortBigFileBenchmarks.sortParallel  avgt    5  21.853 ± 1.232   s/op

    zing
    Benchmark                           Mode  Cnt   Score   Error  Units
    SortBigFileBenchmarks.sort          avgt    5  41.195 ± 1.186   s/op
    SortBigFileBenchmarks.sortParallel  avgt    5  27.285 ± 1.841   s/op


 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@Fork(1)
public class SortBigFileBenchmarks {

    @Setup
    public void setup() {
        try {
            Generator.main("-res AvgNumbers.txt -size 0.5".split(" "));
        }catch (FileNotFoundException | UnsupportedEncodingException e){
            e.printStackTrace();
            throw new RuntimeException("Error while open scanner");
        }
    }

    /* Max array 32768*40 ~ 5MB*/
    @Benchmark
    public void sort() {
        SortBigFile sortBigFile =
                SortBigFile.createSortBigFile(Integer.class)
                        .setMaxChunkLen(2457600)
                        .setMaxCountOfChunks(40)
                        .setInputFileName("AvgNumbers.txt")
                        .setOutputFileName("Out.txt")
                        .setValueScanner(new IntegerScanner())
                        .setCompareStrategy(new IntegerCompareStrategy())
                        .build();

        sortBigFile.sortResults();
    }

    /* Max array 32768*40 ~ 5MB*/
    @Benchmark
    public void sortParallel() {
        SortBigFile sortBigFile =
                SortBigFile.createSortBigFile(Integer.class)
                        .setMaxChunkLen(2457600)
                        .setMaxCountOfChunks(40)
                        .setPoolSize(8)
                        .setInputFileName("AvgNumbers.txt")
                        .setOutputFileName("Out.txt")
                        .setValueScanner(new IntegerScanner())
                        .setCompareStrategy(new IntegerCompareStrategy())
                        .userParallelMerge(true)
                        .build();

        sortBigFile.sortResults();
    }

    public static void main(String[] args) {

        Options opt = new OptionsBuilder()
                .include(SortBigFileBenchmarks.class.getSimpleName())
                .warmupIterations(4)
                .measurementIterations(5)
                .forks(1)
                .jvmArgs("-Xmx500m","-Xms500m")
                .build();
        try {
            new Runner(opt).run();
        }catch(RunnerException e){
            e.printStackTrace();
        }
    }
}
