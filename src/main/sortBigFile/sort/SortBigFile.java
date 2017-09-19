package main.sortBigFile.sort;

import main.sortBigFile.buffers.CyclicBufferHolder;
import main.sortBigFile.readers.ICompareStrategy;
import main.sortBigFile.sort.externalSort.SortFilesPartMemory;
import main.sortBigFile.sort.kWayMerge.MergeFiles;
import main.sortBigFile.sort.kWayMerge.MergeFilesParallel;
import main.sortBigFile.writers.IValueScanner;

import java.io.*;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;

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
    private int poolSize;
    private FileNamesHolder holder;
    private IValueScanner<T> valueScanner;
    private String tempFolderName;
    private String inputFileName;
    private int maxChunkLen;
    private ICompareStrategy<T> compareStrategy;

    private SortBigFile() {
    }

    /**
     * Split input file on maxCountOfChunks and sort them independently
     */
    public void sortResults() {
        sortFilesPartMemory.sortResults();
    }

    /**
     * Merge files which appeared from sortResults step
     */
    public void merge() {
        try {
            while (holder.getSize() > 1) {
                kWayMerge(Integer.min(maxCountOfChunks, holder.getSize()));
            }

            renameFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Merge parallel files which appeared from sortResults step
     *
     * @param maxFileInTask maximum file that can be used during single k-way merge
     */
    public void mergeParallel(int maxFileInTask) {
        MergeFilesParallel mergeFilesParallel = new MergeFilesParallel<>(new CyclicBufferHolder<>(array, maxCountOfChunks), getFilePathToTempFiles(), holder, valueScanner, compareStrategy);
        mergeFilesParallel.merge(maxFileInTask, poolSize);
        renameFile();
    }

    /**
     * Get builder for building class SortBigFile
     *
     * @param cls class of sorting elements
     * @param <E> type of sorting elements
     * @return builder
     */
    public static <E> Builder<E> createSortBigFile(Class<E> cls) {
        return new Builder<E>(cls);
    }

    private void kWayMerge(final int size) throws IOException {
        MergeFiles mergeFiles = new MergeFiles<>(new CyclicBufferHolder<>(array, size), holder, valueScanner, compareStrategy);
        mergeFiles.merge(size, getFilePathToTempFiles());
    }

    private void renameFile() {
        if (holder.getSize() == 1) {
            List<String> fileNames = holder.get(1);

            File file = new File(fileNames.get(0));
            if (!file.renameTo(new File(outputFileName))) {
                System.out.println("Can't rename destination file to " + outputFileName);
            }
        } else {
            System.out.println("Some sort files become unsorted: " + holder.get(holder.getSize()));
        }
    }

    private String getFilePathToTempFiles() {
        return String.format("%s//%s", tempFolderName, outputFileName);
    }

    public static class Builder<T> {
        private SortBigFile<T> sortBigFile;
        private Class<T> cls;

        private Builder(Class<T> cls) {
            this.sortBigFile = new SortBigFile<>();
            this.cls = cls;
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

            sortBigFile.holder = new FileNamesHolder();
            sortBigFile.sortFilesPartMemory = new SortFilesPartMemory<>(sortBigFile.array, sortBigFile.maxCountOfChunks, sortBigFile.maxChunkLen, sortBigFile.getFilePathToTempFiles(), sortBigFile.holder, sortBigFile.poolSize, sortBigFile.inputFileName, sortBigFile.valueScanner);

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
            if (poolSize <= 0)
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
    }
}
