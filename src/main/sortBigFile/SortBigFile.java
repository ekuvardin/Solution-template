package main.sortBigFile;

import main.sortBigFile.writers.IntegerScanner;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SortBigFile<T extends Comparable<T>> {

    private SortFiles<T> sortFiles;
    private final String outputFileName;
    private final int maxCountOfChunks;
    private final T[] array;
    private final AtomicInteger counter = new AtomicInteger();

    public SortBigFile(int maxChunkLen, int maxCountOfChunks, int poolSize, String inputFileName, String outputFileName, Class<T> cls) {
        this.maxCountOfChunks = maxCountOfChunks;
        this.outputFileName = outputFileName;
        array = (T[]) Array.newInstance(cls,maxCountOfChunks * maxChunkLen);
        sortFiles = new SortFiles(array, maxCountOfChunks, maxChunkLen, outputFileName, counter, poolSize, inputFileName, new IntegerScanner());
    }

    public void sortResults() {
        sortFiles.sortResults();
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
        if (start >= end) return;

        MergeFiles mergeFiles = new MergeFiles();
        mergeFiles.merge(start, end, outputFileName, array, outputFileName + counter.incrementAndGet());
    }

}
