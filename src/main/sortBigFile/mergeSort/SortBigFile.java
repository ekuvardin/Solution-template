package main.sortBigFile.mergeSort;

import main.sortBigFile.buffers.CyclicBufferHolder;
import main.sortBigFile.mergeSort.MergeFiles;
import main.sortBigFile.mergeSort.MergeFilesParallel;
import main.sortBigFile.mergeSort.SortFilesPartMemory;
import main.sortBigFile.readers.FileNamesHolder;
import main.sortBigFile.writers.IValueScanner;

import java.io.*;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;

public class SortBigFile<T extends Comparable<T>> {

    private SortFilesPartMemory<T> sortFilesPartMemory;
    private final String outputFileName;
    private final int maxCountOfChunks;
    private final T[] array;
    private final int poolSize;
    private final FileNamesHolder holder;
    private final IValueScanner<T> valueScanner;
    private final String tempFolderName;

    public SortBigFile(int maxChunkLen, int maxCountOfChunks, int poolSize, String inputFileName, String outputFileName, Class<T> cls, IValueScanner<T> valueScanner) {
        if (maxCountOfChunks <= 0)
            throw new IllegalArgumentException("expected maxCountOfChunks must be greater than zero");

        if (poolSize <= 0)
            throw new IllegalArgumentException("expected poolSize must be greater than zero");

        if (inputFileName == null)
            throw new IllegalArgumentException("expected non null inputFileName");

        if (outputFileName == null)
            throw new IllegalArgumentException("expected non null inputFileName");

        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        String time = dateFormat.format(now);
        File dir = new File(time);
        dir.mkdir();

        File file = new File(outputFileName);
        file.delete();

        this.maxCountOfChunks = maxCountOfChunks;
        this.tempFolderName = time;
        this.outputFileName = outputFileName;
        this.poolSize = poolSize;
        this.array = (T[]) Array.newInstance(cls, maxCountOfChunks * maxChunkLen);
        this.holder = new FileNamesHolder();
        this.valueScanner = valueScanner;
        this.sortFilesPartMemory = new SortFilesPartMemory<>(array, maxCountOfChunks, maxChunkLen, getFilePathToTempFiles(), holder, poolSize, inputFileName, valueScanner);
    }

    public void sortResults() {
        sortFilesPartMemory.sortResults();
    }

    public void merge() {
        try {
            while (holder.getSize() > 1) {
                kWayMerge(Integer.min(maxCountOfChunks, holder.getSize()));
            }

            renameFile();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void mergeParallel(int maxChunkInTask) {
        try {
            MergeFilesParallel mergeFilesParallel = new MergeFilesParallel<>(new CyclicBufferHolder<>(array, maxCountOfChunks), getFilePathToTempFiles(), holder, valueScanner);
            mergeFilesParallel.merge(maxChunkInTask, poolSize);
            renameFile();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void kWayMerge(final int size) throws Exception {
        MergeFiles mergeFiles = new MergeFiles<>(new CyclicBufferHolder<>(array, size), holder, valueScanner);
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

}
