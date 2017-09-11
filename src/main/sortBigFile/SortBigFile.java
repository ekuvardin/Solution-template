package main.sortBigFile;

import java.io.*;
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
                new ArrayList<Callable<Integer>>();

        for (int i = 0; i < maxCountOfChunks; i++) {
            final int index = i;
            partitions.add(() -> {
                if (lastPointer[index] != 0) {
                    Arrays.sort(array, index * maxChunkLen, index * maxChunkLen + lastPointer[index] - 1);

                    try (
                            FileWriter fw = new FileWriter(outputFileName + counter.incrementAndGet(), false);
                            BufferedWriter bw = new BufferedWriter(fw);
                            PrintWriter out = new PrintWriter(bw)) {
                        for (int ii = index * maxChunkLen; ii < index * maxChunkLen + lastPointer[index] - 1; ii++) {
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
        try (Scanner scanner = new Scanner(new File(inputFileName));
        ) {
            final List<Callable<Integer>> partitions = initPartitions();
            final ExecutorService executorPool =
                    Executors.newFixedThreadPool(poolSize);


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
                ExecutionException e)

        {
            throw new RuntimeException(e);
        }
    }


    /*
        TODO
        Not working. Do smt with merge.
     */
    public void merge() {
        try {
            for (int j = 1; ; j = j + maxCountOfChunks) {
                merge(j, j + maxCountOfChunks - 1);
                if (j >= counter.get() - 1) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
        TODO
        Not working. Do smt with merge.
     */
    private void merge(final int start, final int end) throws FileNotFoundException, IOException {
        List<MergeReader> scanners = new ArrayList<>(end - start + 1);
        int index = 0;

        for (int fileIndex = start; fileIndex <= end; fileIndex++) {
            File file = new File(outputFileName + fileIndex);
            if (file.exists()) {
                Scanner scanner = new Scanner(file);
                MergeReader mergeReader = new MergeReaderImpl(scanner, array);
                scanners.add(mergeReader);
            }
        }

        int tmpsize = array.length;
        try (FileWriter fw = new FileWriter(outputFileName + counter.incrementAndGet(), false);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            while (scanners.size() > 0) {

                Iterator<MergeReader> w = scanners.iterator();

                int chunk = tmpsize / scanners.size();
                for (int i = 0, s = 0; i < scanners.size(); i++) {
                    MergeReader mergeReader = w.next();
                    mergeReader.fillBuffer(i == scanners.size() - 1 ? tmpsize - s : chunk, s);
                    s = s + chunk;
                }


                Integer min = scanners.stream().min((p1, p2) -> p1.Last().compareTo(p2.Last())).get().Last();
                Arrays.sort(array, 0, maxChunkLen * scanners.size() - 1);


                for (int i = 0; i < array.length; i++) {
                    if (min.compareTo(array[i]) > 1) {
                        tmpsize = i;
                        break;
                    }
                    out.println(array[i]);
                }

                scanners.removeIf(p -> p.isEmpty());
            }
        }
    }
            /*
            List<MergeReader> scannersTmp = new ArrayList<>(scanners);

            try (FileWriter fw = new FileWriter(outputFileName + counter.incrementAndGet(), true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {
                while (scannersTmp.size() != 0) {
                    scannersTmp.parallelStream().forEach(MergeReader::fillBuffer);
                    scannersTmp.removeIf(p -> p.isEmpty());

                    Integer min = scannersTmp.stream().min((p1, p2) -> p1.first().compareTo(p2.first())).get().first();
                    int sum = scannersTmp.stream().mapToInt(p -> p.readUntil(min)).sum();

                    for (int i = 0; i < sum; i++) {
                        out.println(min);
                    }


                }
            }
        } finally {
            clean(scanners);
        }
    }

    public void clean(List<MergeReader> scanners) {
        try {
            for (MergeReader mergeReader : scanners) {
                mergeReader.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
/*

        Integer min = Integer.MAX_VALUE;
        <Integer > res = new TreeSet<>();
        ArrayDeque
        for (Map.Entry<Integer, Scanner> entry : set) {
            Scanner scanner = entry.getValue();
            if (scanner.hasNext()) {
                res.add(scanner.nextInt());
            }
        }

        while ()
            res.first();
        for (int fileIndex = start, min = start; fileIndex <= end; fileIndex++) {

        }
        int min = start;

        int lastPointer = 0;
        for (int fileIndex = start, index = 0; fileIndex <= end; fileIndex++) {
            lastPointer = 0;
            File file = new File(outputFileName + fileIndex);
            if (file.exists()) {
                try (Scanner scanner = new Scanner(file)) {
                    for (; scanner.hasNextInt(); lastPointer++) {
                        array[lastPointer] = scanner.nextInt();
                    }
                }
            }
        }

        Arrays.sort(array, 0, lastPointer - 1);*/


}
