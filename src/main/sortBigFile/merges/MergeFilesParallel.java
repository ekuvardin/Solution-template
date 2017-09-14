package main.sortBigFile.merges;

import main.sortBigFile.FileNamesHolder;
import main.sortBigFile.buffers.CyclicBufferHolder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;

public class MergeFilesParallel {

    private final CyclicBufferHolder cyclicBufferHolder;
    private final String outputFileName;
    private final FileNamesHolder holder;

    public MergeFilesParallel(CyclicBufferHolder cyclicBufferHolder, String outputFileName, FileNamesHolder holder) {
        this.cyclicBufferHolder = cyclicBufferHolder;
        this.outputFileName = outputFileName;
        this.holder = holder;
    }

    // TODO
    // I don't know why when uncomment condition in MergeReducer.onCompletion and removes cycle below
    // then program hangs
    public void merge(int size, int maxChunkInTask, int poolSize) throws IOException {
        while (holder.getSize() > 1) {
            MergeReducer mergeReducer = new MergeReducer(null, holder.getSize(), maxChunkInTask);
            new ForkJoinPool(poolSize).invoke(mergeReducer);
        }
    }


    class MergeReducer extends CountedCompleter<Void> {

        private final int size;
        private final int maxChunkInTask;
        private List<MergeReducer> forks;

        public MergeReducer(CountedCompleter<Void> parent, int size, int maxChunkInTask) {
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
             /*   for (MergeReducer subTask : forks) {
                    subTask.join();
                }

                execMerge(forks.size());*/
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
                MergeFiles mergeFiles = new MergeFiles(cyclicBufferHolder, holder);
                mergeFiles.merge(size, outputFileName);
            } catch (IOException e) {
                e.printStackTrace();
                this.cancel(true);
            }
        }
    }
}