package main.sortBigFile.merges;

import main.sortBigFile.FileNamesHolder;
import main.sortBigFile.buffers.CyclicBufferHolder;
import main.sortBigFile.writers.IValueScanner;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;

public class MergeFilesParallel<T extends Comparable<T>> {

    private final CyclicBufferHolder<T> cyclicBufferHolder;
    private final String outputFileName;
    private final FileNamesHolder holder;
    private final IValueScanner<T> valueScanner;

    public MergeFilesParallel(CyclicBufferHolder<T> cyclicBufferHolder, String outputFileName, FileNamesHolder holder, IValueScanner<T> valueScanner) {
        this.cyclicBufferHolder = cyclicBufferHolder;
        this.outputFileName = outputFileName;
        this.holder = holder;
        this.valueScanner = valueScanner;
    }

    public void merge(int maxChunkInTask, int poolSize) throws IOException {
            MergeReducer mergeReducer = new MergeReducer(null, holder.getSize(), maxChunkInTask);
            new ForkJoinPool(poolSize).invoke(mergeReducer);
    }


    class MergeReducer extends CountedCompleter<Void> {

        private final int size;
        private final int maxChunkInTask;
        private List<MergeReducer> forks;

        private MergeReducer(CountedCompleter<Void> parent, int size, int maxChunkInTask) {
            super(parent);
            this.size = size;
            this.maxChunkInTask = maxChunkInTask;
            this.forks = new ArrayList<>(maxChunkInTask);
        }

        @Override
        public void compute() {
            if (size <= maxChunkInTask) {
                execMerge(size);
            } else {
                int delta = size / maxChunkInTask;
                if (delta <= maxChunkInTask) {
                    delta = maxChunkInTask;
                }

                for (int newSize = delta; newSize < size; newSize = newSize + delta) {
                    addTask(delta);
                }

                int rem = size % maxChunkInTask;
                if (rem != 1) {
                    addTask(rem == 0 ? delta : rem);
                }


            }

            tryComplete();
        }

        @Override
        public void onCompletion(CountedCompleter<?> caller) {
            if (caller != this) {
                execMerge(forks.size());
            }
        }

        private void addTask(int curSize) {
            this.addToPendingCount(1);
            MergeReducer mapReducer = new MergeReducer(this, curSize, maxChunkInTask);
            mapReducer.fork();
            forks.add(mapReducer);
        }

        private void execMerge(int size) {
            try {
                MergeFiles mergeFiles = new MergeFiles<>(cyclicBufferHolder, holder, valueScanner);
                mergeFiles.merge(size, outputFileName);
            } catch (Exception e) {
                e.printStackTrace();
                this.cancel(true);
            }
        }
    }
}