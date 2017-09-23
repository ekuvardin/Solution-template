package main.sortBigFile.sort.kWayMerge;

import main.sortBigFile.sort.FileNamesHolder;
import main.sortBigFile.buffers.CyclicBufferHolder;
import main.sortBigFile.readers.ICompareStrategy;
import main.sortBigFile.writers.IValueScanner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

/**
 * Merge file in parallel using concurrent running several k-way merge
 *
 * @param <T> type of sorting elements
 */
public class MultipleThreadMerge<T> extends SingleThreadMerge<T> {

    private final int poolSize;
    private final int maxFileInTask;

    public MultipleThreadMerge(CyclicBufferHolder<T> cyclicBufferHolder, IValueScanner<T> valueScanner, ICompareStrategy<T> compareStrategy, int poolSize, int maxFileInTask) {
        super(cyclicBufferHolder, valueScanner, compareStrategy);
        this.poolSize = poolSize;
        this.maxFileInTask = maxFileInTask;
    }

    @Override
    public String merge(final List<String> fileNames, String outputFilePath) {
        try {
            MergeReducer mergeReducer = new MergeReducer(null, fileNames, Integer.min(maxFileInTask, fileNames.size()),outputFilePath);
            new ForkJoinPool(poolSize).invoke(mergeReducer);
            return mergeReducer.get();
        } catch (InterruptedException| ExecutionException e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Class splits k-way merge until max file in task becomes<=maxFileInTask.
     * Then wait until sibling tasks execute and collects result.
     * Do k-way merge using files from sibling tasks.
     */
    class MergeReducer extends CountedCompleter<String> {

        private final List<String> fileNames;
        private final int maxFileInTask;
        private List<MergeReducer> forks;
        private String result;
        private final String outputFileName;

        private MergeReducer(CountedCompleter<String> parent, List<String> fileNames, int maxChunkInTask, String outputFileName) {
            super(parent);
            this.fileNames = fileNames;
            this.maxFileInTask = maxChunkInTask;
            this.forks = new ArrayList<>(maxChunkInTask);
            this.outputFileName = outputFileName;
        }

        @Override
        public String getRawResult() {
            return result;
        }

        @Override
        public void compute() {
            final int size = fileNames.size();
            if (size <= maxFileInTask) {
                result = execMerge(fileNames, outputFileName);
            } else {
                int delta = size / maxFileInTask;
                if (delta <= maxFileInTask) {
                    delta = maxFileInTask;
                }

                for (int newSize = delta; newSize < size; newSize = newSize + delta) {
                    addTask(fileNames.subList(newSize - delta, newSize));
                }

                int rem = size % maxFileInTask;
                // If we have only one file then we can't do anything.
                if (rem != 1) {
                    addTask(rem == 0 ? fileNames.subList(size - delta, size) : fileNames.subList(size - rem, size));
                } else {
                    result = fileNames.get(size - 1);
                    forks.add(this);
                }
            }

            tryComplete();
        }

        @Override
        public void onCompletion(CountedCompleter<?> caller) {
            if (caller != this) {
                List<String> fileTmp = forks.stream().map(MergeReducer::getRawResult).collect(Collectors.toList());
                result = execMerge(fileTmp, outputFileName);
            }
        }

        private void addTask(List<String> fileTmp) {
            this.addToPendingCount(1);
            MergeReducer mapReducer = new MergeReducer(this, fileTmp, maxFileInTask, outputFileName);
            mapReducer.fork();
            forks.add(mapReducer);
        }

        private String execMerge(List<String> fileNames, String outputFileName) {
            try {
                return kWayMerge(fileNames, FileNamesHolder.getNewUniqueName(outputFileName));
            } catch (IOException e) {
                e.printStackTrace();
                this.cancel(true);
                return null;
            }
        }
    }
}