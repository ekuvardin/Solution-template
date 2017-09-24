package main.sortBigFile.sort.externalSort;

import main.sortBigFile.sort.FileNamesHolder;
import main.sortBigFile.writers.IValueScanner;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
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

    private List<Callable<Void>> initPartitions(final Scanner scanner, final List<String> fileNames) throws ExecutionException, InterruptedException {
        final List<Callable<Void>> partitions =
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

                        fileNames.add(fileName);
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

        for (Future<Void> future : resultFromParts) {
            future.get();
        }
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
     *
     * @return return created files
     */
    public List<String> sortResults() {
        final ExecutorService executorPool =
                Executors.newFixedThreadPool(poolSize);

        try (Scanner scanner = new Scanner(new File(inputFileName), StandardCharsets.UTF_8.toString())) {
            final List<String> fileNames = Collections.synchronizedList(new ArrayList<>(maxCountOfChunks));
            final List<Callable<Void>> partitions = initPartitions(scanner, fileNames);
            run(executorPool, partitions);
            return fileNames;
        } catch (InterruptedException | IOException |
                ExecutionException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            executorPool.shutdownNow();
        }
    }
}
