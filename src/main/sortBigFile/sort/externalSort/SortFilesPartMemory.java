package main.sortBigFile.sort.externalSort;

import main.sortBigFile.sort.FileNamesHolder;
import main.sortBigFile.writers.IValueScanner;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

/**
 * Sort input file by splitting them to smaller
 *
 * @param <T> type of sorting elements
 */
public class SortFilesPartMemory<T> {

    private final T[] array;
    private final int maxCountOfChunks;
    private final int maxChunkLen;
    private final int poolSize;
    private final String outputFileName;
    private final String inputFileName;
    private final IValueScanner<T> valueScanner;

    public SortFilesPartMemory(T[] array, int maxCountOfChunks, int maxChunkLen, String outputFileName, int poolSize, String inputFileName, IValueScanner<T> valueScanner) {
        this.array = array;
        this.maxCountOfChunks = maxCountOfChunks;
        this.maxChunkLen = maxChunkLen;
        this.outputFileName = outputFileName;
        this.inputFileName = inputFileName;
        this.poolSize = poolSize;
        this.valueScanner = valueScanner;
    }

    private List<Callable<String>> initPartitions(final Scanner scanner) throws ExecutionException, InterruptedException {
        final List<Callable<String>> partitions =
                new ArrayList<>(maxCountOfChunks);

        for (int i = 0; i < maxCountOfChunks; i++) {
            final int index = i;
            partitions.add(() -> {
                final int startKey = index * maxChunkLen;
                int stopKey = startKey;
                while ((stopKey = fillBuffer(scanner, startKey, Integer.min((index + 1) * maxChunkLen, array.length))) > startKey) {
                    Arrays.sort(array, startKey, stopKey);

                    String fileName = FileNamesHolder.getNewUniqueName(outputFileName);
                    try (
                            FileOutputStream fileOutputStream = new FileOutputStream(new File(fileName));
                            OutputStreamWriter fw = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
                            BufferedWriter bw = new BufferedWriter(fw);
                            PrintWriter out = new PrintWriter(bw)) {
                        for (int ii = startKey; ii < stopKey; ii++)
                            out.println(array[ii]);

                        return fileName;
                    }
                }
                return null;
            });
        }

        return partitions;
    }

    private List<String> run(ExecutorService executorPool, List<Callable<String>> partitions) throws InterruptedException, ExecutionException {
        final List<Future<String>> resultFromParts =
                executorPool.invokeAll(partitions, 100000, TimeUnit.SECONDS);

        List<String> res = new ArrayList<>(partitions.size());
        for (Future<String> future : resultFromParts) {
            res.add(future.get());
        }

        return res;
    }

    private int fillBuffer(Scanner scanner, int start, int stop) {
        int lastPointer = start;
        synchronized (scanner) {
            for (; lastPointer < stop && valueScanner.hasNext(scanner); lastPointer++)
                array[lastPointer] = valueScanner.nextValue(scanner);
        }
        return lastPointer;
    }

    /**
     * Split input file on maxCountOfChunks and sort them independently
     */
    public List<String> sortResults() {
        final ExecutorService executorPool =
                Executors.newFixedThreadPool(poolSize);

        try (Scanner scanner = new Scanner(new File(inputFileName), StandardCharsets.UTF_8.toString())) {
            final List<Callable<String>> partitions = initPartitions(scanner);
            return run(executorPool, partitions);
        } catch (InterruptedException | IOException |
                ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            executorPool.shutdownNow();
        }
    }
}
