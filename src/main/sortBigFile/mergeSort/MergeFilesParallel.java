package main.sortBigFile.mergeSort;

import main.sortBigFile.readers.FileNamesHolder;
import main.sortBigFile.buffers.CyclicBufferHolder;
import main.sortBigFile.writers.IValueScanner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;

/**
 * Merge file in parallel using concurrent running several k-way merge
 *
 * @param <T> type of sorting elements
 */
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

    /**
     * Start parallel merge
     *
     * @param maxFileInTask max file count taking part in one k-way merge
     * @param poolSize max pool size
     */
    public void merge(int maxFileInTask, int poolSize) {
            MergeReducer mergeReducer = new MergeReducer(null, holder.getSize(), Integer.min(maxFileInTask, holder.getSize()));
            new ForkJoinPool(poolSize).invoke(mergeReducer);
    }

    /**
     * Class splits k-way merge until max file in task becomes<=maxFileInTask.
     * Then wait until sibling tasks execute and collects result.
     * Do k-way merge using files from sibling tasks.
     */
    class MergeReducer extends CountedCompleter<Void> {

        private final int size;
        private final int maxFileInTask;
        private List<MergeReducer> forks;

        private MergeReducer(CountedCompleter<Void> parent, int size, int maxChunkInTask) {
            super(parent);
            this.size = size;
            this.maxFileInTask = maxChunkInTask;
            this.forks = new ArrayList<>(maxChunkInTask);
        }

        @Override
        public void compute() {
            if (size <= maxFileInTask) {
                execMerge(size);
            } else {
                int delta = size / maxFileInTask;
                if (delta <= maxFileInTask) {
                    delta = maxFileInTask;
                }

                for (int newSize = delta; newSize < size; newSize = newSize + delta) {
                    addTask(delta);
                }

                int rem = size % maxFileInTask;
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
            MergeReducer mapReducer = new MergeReducer(this, curSize, maxFileInTask);
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