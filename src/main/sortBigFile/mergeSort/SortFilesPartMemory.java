package main.sortBigFile.mergeSort;

import main.sortBigFile.readers.FileNamesHolder;
import main.sortBigFile.writers.IValueScanner;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

public class SortFilesPartMemory<T extends Comparable<T>> {

    private final T[] array;
    private final int maxCountOfChunks;
    private final int maxChunkLen;
    private final int poolSize;
    private final int[] lastPointer;
    private final String outputFileName;
    private final String inputFileName;
    private FileNamesHolder holder;
    private final IValueScanner<T> valueScanner;

    public SortFilesPartMemory(T[] array, int maxCountOfChunks, int maxChunkLen, String outputFileName, FileNamesHolder holder, int poolSize, String inputFileName, IValueScanner<T> valueScanner) {
        this.array = array;
        this.maxCountOfChunks = maxCountOfChunks;
        this.lastPointer = new int[maxCountOfChunks];
        this.maxChunkLen = maxChunkLen;
        this.outputFileName = outputFileName;
        this.holder = holder;
        this.inputFileName = inputFileName;
        this.poolSize = poolSize;
        this.valueScanner = valueScanner;
    }

    private List<Callable<Void>> initPartitions() throws ExecutionException, InterruptedException {
        final List<Callable<Void>> partitions =
                new ArrayList<>();

        for (int i = 0; i < maxCountOfChunks; i++) {
            final int index = i;
            partitions.add(() -> {
                if (lastPointer[index] != 0) {
                    Arrays.sort(array, index * maxChunkLen, index * maxChunkLen + lastPointer[index]);

                    String fileName = holder.getNewUniqueName(outputFileName);
                    try (
                            FileWriter fw = new FileWriter( fileName, false);
                            BufferedWriter bw = new BufferedWriter(fw);
                            PrintWriter out = new PrintWriter(bw)) {
                        for (int ii = index * maxChunkLen; ii < index * maxChunkLen + lastPointer[index]; ii++) {
                            out.println(array[ii]);
                        }
                    } finally{
                        holder.pull(fileName);
                    }

                }
                return null;
            });
        }

        return partitions;
    }

    private void run(ExecutorService executorPool, List<Callable<Void>> partitions) throws InterruptedException, ExecutionException {
        final List<Future<Void>> resultFromParts =
                executorPool.invokeAll(partitions, 100000, TimeUnit.SECONDS);
        for (Future<Void> result : resultFromParts) {
            result.get();
        }

    }

    public void sortResults() {

        final ExecutorService executorPool =
                Executors.newFixedThreadPool(poolSize);

        try (Scanner scanner = new Scanner(new File(inputFileName), StandardCharsets.UTF_8.toString())) {
            final List<Callable<Void>> partitions = initPartitions();

            while (scanner.hasNextInt()) {
                Arrays.fill(lastPointer, 0);

                int lastPointer = 0;
                for (; lastPointer < array.length && valueScanner.hasNext(scanner); lastPointer++) {
                    array[lastPointer] = valueScanner.nextValue(scanner);
                    if (lastPointer % maxChunkLen == 0) {
                        this.lastPointer[lastPointer / maxChunkLen] = maxChunkLen;
                    }
                }

                if (lastPointer / maxChunkLen < maxCountOfChunks) {
                    this.lastPointer[lastPointer / maxChunkLen] = lastPointer % maxChunkLen;
                }

                run(executorPool, partitions);

            }
        } catch (InterruptedException | IOException |
                ExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            executorPool.shutdownNow();
        }
    }
}
