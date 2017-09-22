package main.sortBigFile.sort;

import main.sortBigFile.buffers.CyclicBufferHolder;
import main.sortBigFile.readers.ICompareStrategy;
import main.sortBigFile.sort.externalSort.SortFilesPartMemory;
import main.sortBigFile.sort.kWayMerge.MergeFiles;
import main.sortBigFile.sort.kWayMerge.MergeFilesParallel;
import main.sortBigFile.writers.IValueScanner;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Date;
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
    private int poolSize;
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
    public List<String> sortResults() {
        return sortFilesPartMemory.sortResults();
    }

    /**
     * Merge files which appeared from sortResults step
     */
    public void merge(final List<String> fileNames) {
        if(fileNames == null || fileNames.isEmpty())
            return;

        try {
            List<String> newList = new ArrayList<>(fileNames);


            while (newList.size() > 1) {
                List<String> temp = newList.subList(0, Integer.min(maxCountOfChunks, newList.size()));
                newList.add(kWayMerge(temp));

                for(int i=0;i<newList.size()-1;i++)
                    newList.remove(0);
            }

            renameFile(newList.get(0));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Merge parallel files which appeared from sortResults step
     *
     * @param maxFileInTask maximum file that can be used during single k-way merge
     */
    public void mergeParallel(int maxFileInTask, List<String> fileNames)  {
        try {
            MergeFilesParallel mergeFilesParallel = new MergeFilesParallel<>(new CyclicBufferHolder<>(array, maxCountOfChunks), getFilePathToTempFiles(), valueScanner, compareStrategy, fileNames);
            renameFile(mergeFilesParallel.merge(maxFileInTask, poolSize));
        } catch(InterruptedException | ExecutionException e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
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

    private String kWayMerge(List<String> fileNames) throws IOException {
        MergeFiles mergeFiles = new MergeFiles<>(new CyclicBufferHolder<>(array, fileNames.size()), valueScanner, compareStrategy, fileNames);
        String newName = FileNamesHolder.getNewUniqueName(getFilePathToTempFiles());
        mergeFiles.merge(newName);
        return newName;
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

            sortBigFile.sortFilesPartMemory = new SortFilesPartMemory<>(sortBigFile.array, sortBigFile.maxCountOfChunks, sortBigFile.maxChunkLen, sortBigFile.getFilePathToTempFiles(), sortBigFile.poolSize, sortBigFile.inputFileName, sortBigFile.valueScanner);

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
