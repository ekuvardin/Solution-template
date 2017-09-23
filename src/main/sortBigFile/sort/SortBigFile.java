package main.sortBigFile.sort;

import java.util.ArrayDeque;

import main.sortBigFile.buffers.CyclicBufferHolder;
import main.sortBigFile.readers.ICompareStrategy;
import main.sortBigFile.sort.externalSort.SortFilesPartMemory;
import main.sortBigFile.sort.kWayMerge.IMergeStrategy;
import main.sortBigFile.sort.kWayMerge.MergeFiles;
import main.sortBigFile.sort.kWayMerge.MultipleThreadMerge;
import main.sortBigFile.sort.kWayMerge.SingleThreadMerge;
import main.sortBigFile.writers.IValueScanner;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutionException;

/**
 * Sort big file
 *
 * @param <T> type of sorting elements
 */
public class SortBigFile<T> {

    private SortFilesPartMemory<T> sortFilesPartMemory;
    private String outputFileName;
    private int maxCountOfChunks;
    private T[] array;
    private int poolSize = 1;
    private IValueScanner<T> valueScanner;
    private String tempFolderName;
    private String inputFileName;
    private int maxChunkLen;
    private ICompareStrategy<T> compareStrategy;
    private IMergeStrategy mergeStrategy;

    private SortBigFile() {
    }

    /**
     * Split input file on maxCountOfChunks and sort them independently
     */
    public void sortResults() {
        String resultFile = mergeStrategy.merge(sortFilesPartMemory.sortResults(), getFilePathToTempFiles());
        renameFile(resultFile);
    }

    /**
     * Get builder for building class SortBigFile
     *
     * @param cls class of sorting elements
     * @param <E> type of sorting elements
     * @return builder
     */
    public static <E> Builder<E> createSortBigFile(Class<E> cls) {
        return new Builder<>(cls);
    }

    private void renameFile(String lastFile) {
        if (lastFile != null) {
            File file = new File(lastFile);
            if (!file.renameTo(new File(outputFileName)))
                System.out.println("Can't rename destination file to " + outputFileName);
        } else {
            System.out.println("Last file is null");
        }
    }

    private String getFilePathToTempFiles() {
        return String.format("%s//%s", tempFolderName, outputFileName);
    }

    public static class Builder<T> {
        private SortBigFile<T> sortBigFile;
        private Class<T> cls;
        boolean userParallel;

        private Builder(Class<T> cls) {
            this.sortBigFile = new SortBigFile<>();
            this.cls = cls;
            userParallel = false;
        }

        public SortBigFile<T> build() {
            sortBigFile.array = (T[]) Array.newInstance(cls, sortBigFile.maxCountOfChunks * sortBigFile.maxChunkLen);
            Date now = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
            String time = dateFormat.format(now);
            File dir = new File(time);
            dir.mkdir();

            sortBigFile.tempFolderName = time;

            File file = new File(sortBigFile.outputFileName);
            file.delete();

            sortBigFile.sortFilesPartMemory = new SortFilesPartMemory<>(sortBigFile.array, sortBigFile.maxCountOfChunks, sortBigFile.maxChunkLen, sortBigFile.getFilePathToTempFiles(), sortBigFile.poolSize, sortBigFile.inputFileName, sortBigFile.valueScanner);

            if (!userParallel || sortBigFile.poolSize <= 1)
                sortBigFile.mergeStrategy = new SingleThreadMerge<>(new CyclicBufferHolder<T>(sortBigFile.array, sortBigFile.maxCountOfChunks), sortBigFile.valueScanner, sortBigFile.compareStrategy);
            else
                sortBigFile.mergeStrategy = new MultipleThreadMerge<T>(new CyclicBufferHolder<T>(sortBigFile.array, sortBigFile.maxCountOfChunks), sortBigFile.valueScanner, sortBigFile.compareStrategy, sortBigFile.poolSize, sortBigFile.maxCountOfChunks / sortBigFile.poolSize);

            SortBigFile<T> value = sortBigFile;
            sortBigFile = null;
            return value;
        }

        public Builder<T> setMaxChunkLen(int maxChunkLen) {
            if (maxChunkLen <= 0)
                throw new IllegalArgumentException("expected maxCountOfChunks must be greater than zero");

            sortBigFile.maxChunkLen = maxChunkLen;
            return this;
        }

        public Builder<T> setMaxCountOfChunks(int maxCountOfChunks) {
            if (maxCountOfChunks <= 0)
                throw new IllegalArgumentException("expected maxCountOfChunks must be greater than zero");

            sortBigFile.maxCountOfChunks = maxCountOfChunks;
            return this;
        }

        public Builder<T> setOutputFileName(String outputFileName) {
            if (outputFileName == null)
                throw new IllegalArgumentException("expected non null outputFileName");

            sortBigFile.outputFileName = outputFileName;
            return this;
        }

        public Builder<T> setInputFileName(String inputFileName) {
            if (inputFileName == null)
                throw new IllegalArgumentException("expected non null inputFileName");

            sortBigFile.inputFileName = inputFileName;
            return this;
        }

        public Builder<T> setPoolSize(int poolSize) {
            if (poolSize <= 1)
                throw new IllegalArgumentException("expected poolSize must be greater than zero");

            sortBigFile.poolSize = poolSize;
            return this;
        }

        public Builder<T> setValueScanner(IValueScanner<T> valueScanner) {
            sortBigFile.valueScanner = valueScanner;
            return this;
        }

        public Builder<T> setCompareStrategy(ICompareStrategy<T> compareStrategy) {
            sortBigFile.compareStrategy = compareStrategy;
            return this;
        }

        public Builder<T> userParallelMerge(boolean userParallelMerge) {
            userParallel = userParallelMerge;
            return this;
        }
    }
}
