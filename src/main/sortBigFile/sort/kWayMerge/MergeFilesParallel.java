package main.sortBigFile.sort.kWayMerge;

import main.sortBigFile.sort.FileNamesHolder;
import main.sortBigFile.buffers.CyclicBufferHolder;
import main.sortBigFile.readers.ICompareStrategy;
import main.sortBigFile.writers.IValueScanner;

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
public class MergeFilesParallel<T> {

    private final CyclicBufferHolder<T> cyclicBufferHolder;
    private final String outputFileName;
    private final IValueScanner<T> valueScanner;
    private final ICompareStrategy<T> compareStrategy;
    private final List<String> fileNames;

    public MergeFilesParallel(CyclicBufferHolder<T> cyclicBufferHolder, String outputFileName, IValueScanner<T> valueScanner, ICompareStrategy<T> compareStrategy, List<String> fileNames) {
        this.cyclicBufferHolder = cyclicBufferHolder;
        this.outputFileName = outputFileName;
        this.valueScanner = valueScanner;
        this.compareStrategy = compareStrategy;
        this.fileNames = fileNames;
    }

    /**
     * Start parallel merge
     *
     * @param maxFileInTask max file count taking part in one k-way merge
     * @param poolSize      max pool size
     */
    public String merge(int maxFileInTask, int poolSize) throws InterruptedException, ExecutionException {
        MergeReducer mergeReducer = new MergeReducer(null, fileNames, Integer.min(maxFileInTask, fileNames.size()));
        new ForkJoinPool(poolSize).invoke(mergeReducer);
        return mergeReducer.get();
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

        private MergeReducer(CountedCompleter<String> parent, List<String> fileNames, int maxChunkInTask) {
            super(parent);
            this.fileNames = fileNames;
            this.maxFileInTask = maxChunkInTask;
            this.forks = new ArrayList<>(maxChunkInTask);
        }

        public String getResult() {
            return result;
        }

        @Override
        public void compute() {
            final int size = fileNames.size();
            if (size <= maxFileInTask) {
                result = execMerge(fileNames);
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
                List<String> fileTmp = forks.stream().map(MergeReducer::getResult).collect(Collectors.toList());
                result = execMerge(fileTmp);
            }
        }

        private void addTask(List<String> fileTmp) {
            this.addToPendingCount(1);
            MergeReducer mapReducer = new MergeReducer(this, fileTmp, maxFileInTask);
            mapReducer.fork();
            forks.add(mapReducer);
        }

        private String execMerge(List<String> fileNames) {
            try {
                MergeFiles mergeFiles = new MergeFiles<>(cyclicBufferHolder, valueScanner, compareStrategy, fileNames);
                String newName = FileNamesHolder.getNewUniqueName(outputFileName);
                mergeFiles.merge(newName);
                return newName;
            } catch (Exception e) {
                e.printStackTrace();
                this.cancel(true);
                return null;
            }
        }
    }
}