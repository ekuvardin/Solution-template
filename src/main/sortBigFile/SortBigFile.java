package main.sortBigFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SortBigFile {

    private final int maxChunkLen;
    private final String inputFileName;
    private final String outputFileName;
    private final int maxCountOfChunks;
    private final int poolSize;
    private final Integer[] array;
    private final Integer[] lastPointer;
    private final AtomicInteger counter = new AtomicInteger();

    public SortBigFile(int maxChunkLen, int maxCountOfChunks, int poolSize, String inputFileName, String outputFileName) {
        this.maxChunkLen = maxChunkLen;
        this.inputFileName = inputFileName;
        this.maxCountOfChunks = maxCountOfChunks;
        this.poolSize = poolSize;
        this.outputFileName = outputFileName;
        array = new Integer[maxCountOfChunks * maxChunkLen];
        lastPointer = new Integer[maxCountOfChunks];
    }

    private List<Callable<Integer>> initPartitions() throws ExecutionException, InterruptedException {
        final List<Callable<Integer>> partitions =
                new ArrayList<>();

        for (int i = 0; i < maxCountOfChunks; i++) {
            final int index = i;
            partitions.add(() -> {
                if (lastPointer[index] != 0) {
                    Arrays.sort(array, index * maxChunkLen, index * maxChunkLen + lastPointer[index]);

                    try (
                            FileWriter fw = new FileWriter(outputFileName + counter.incrementAndGet(), false);
                            BufferedWriter bw = new BufferedWriter(fw);
                            PrintWriter out = new PrintWriter(bw)) {
                        for (int ii = index * maxChunkLen; ii < index * maxChunkLen + lastPointer[index]; ii++) {
                            out.println(array[ii]);
                        }
                    }

                }
                return 1;
            });
        }

        return partitions;
    }

    private void run(ExecutorService executorPool, List<Callable<Integer>> partitions) throws InterruptedException, ExecutionException {
        final List<Future<Integer>> resultFromParts =
                executorPool.invokeAll(partitions, 100000, TimeUnit.SECONDS);
        for (Future<Integer> result : resultFromParts) {
            result.get();
        }

    }

    public void sortResults() {

        final ExecutorService executorPool =
                Executors.newFixedThreadPool(poolSize);

        try (Scanner scanner = new Scanner(new File(inputFileName), StandardCharsets.UTF_8.toString())) {
            final List<Callable<Integer>> partitions = initPartitions();

            while (scanner.hasNextInt()) {
                Arrays.fill(lastPointer, 0);

                int lastPointer = 0;
                for (; lastPointer < array.length && scanner.hasNextInt(); lastPointer++) {
                    array[lastPointer] = scanner.nextInt();
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

    public void merge() {
        try {
            for (int j = 1; ; j = j + maxCountOfChunks) {
                kWayMerge(j, Integer.min(j + maxCountOfChunks - 1, counter.get()));
                if (j + maxCountOfChunks >= counter.get() - 1) {
                    break;
                }
            }
            File file = new File(outputFileName + counter.get());
            if(!file.renameTo(new File(outputFileName))){
                System.out.println("Can't rename destination file to "+outputFileName);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void kWayMerge(final int start, final int end) throws IOException {
        if (start >= end) {
            return;
        }
        MergeFiles mergeFiles = new MergeFiles();
        mergeFiles.merge(start, end, outputFileName, array, outputFileName + counter.incrementAndGet());
    }

}
