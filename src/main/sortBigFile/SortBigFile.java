package main.sortBigFile;

import main.sortBigFile.buffers.CyclicBufferHolder;
import main.sortBigFile.merges.MergeFiles;
import main.sortBigFile.merges.MergeFilesParallel;
import main.sortBigFile.writers.IValueScanner;

import java.io.*;
import java.lang.reflect.Array;

public class SortBigFile<T extends Comparable<T>> {

    private SortFiles<T> sortFiles;
    private final String outputFileName;
    private final int maxCountOfChunks;
    private final T[] array;
    private final int poolSize;
    private final FileNamesHolder holder;
    private final IValueScanner<T> scanner;

    public SortBigFile(int maxChunkLen, int maxCountOfChunks, int poolSize, String inputFileName, String outputFileName, Class<T> cls, IValueScanner<T> scanner) {
        this.maxCountOfChunks = maxCountOfChunks;
        this.outputFileName = outputFileName;
        this.poolSize = poolSize;
        this.array = (T[]) Array.newInstance(cls, maxCountOfChunks * maxChunkLen);
        this.holder = new FileNamesHolder();
        this.scanner = scanner;
        this.sortFiles = new SortFiles<>(array, maxCountOfChunks, maxChunkLen, outputFileName, holder, poolSize, inputFileName, scanner);
    }

    public void sortResults() {
        sortFiles.sortResults();
    }

    public void merge() {
        try {
            while(holder.getSize() > 1){
                kWayMerge(Integer.min(maxCountOfChunks, holder.getSize()));
            }

            File file = new File(holder.get(1).get(0));
            if (!file.renameTo(new File(outputFileName))) {
                System.out.println("Can't rename destination file to " + outputFileName);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void mergeParallel(int maxChunkInTask) {
        try {
            MergeFilesParallel mergeFilesParallel = new MergeFilesParallel<>(new CyclicBufferHolder<>(array, maxCountOfChunks), outputFileName, holder, scanner);
            mergeFilesParallel.merge(holder.getSize(), maxChunkInTask, poolSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void kWayMerge(final int size) throws IOException {
        MergeFiles mergeFiles = new MergeFiles<>(new CyclicBufferHolder<>(array, size), holder, scanner);
        mergeFiles.merge(size, outputFileName);
    }

}
