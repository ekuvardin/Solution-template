package main.sortBigFile.readers;

import main.sortBigFile.buffers.ICyclicBuffer;
import main.sortBigFile.buffers.Sections;

import java.io.PrintWriter;
import java.util.*;

/**
 * Read arrays, make k-way merge and write to file
 *
 * @param <T> type of sorting elements
 */
public class MergeArrayReader<T> implements IMergeArrayReader {

    private Sections<T> sections;
    private ICompareStrategy<T> compareStrategy;

    public MergeArrayReader(Sections<T> sections, ICompareStrategy<T> compareStrategy) {
        this.sections = sections;
        this.compareStrategy = compareStrategy;
    }

    @Override
    public void mergeTillEmpty(PrintWriter out) {
        List<ICyclicBuffer<T>> tmpCol = new ArrayList<>(sections.getBuffers());
        tmpCol.removeIf(p -> p.getSize() == 0);

        mergeTillStopKey(out, null, tmpCol);
    }

    @Override
    public void mergeTillMinMax(PrintWriter out) {
        List<ICyclicBuffer<T>> tmpCol = new ArrayList<>(sections.getBuffers());
        tmpCol.removeIf(p -> p.getSize() == 0);

        T minMax = tmpCol.stream().map(ICyclicBuffer::getLast).min(compareStrategy::compareTo).orElse(null);
        mergeTillStopKey(out, minMax, tmpCol);
    }

    private void mergeTillStopKey(PrintWriter out, T minMax, List<ICyclicBuffer<T>> tmpCol) {
        Optional<T> min = tmpCol.stream().map(ICyclicBuffer::getFirst).min(compareStrategy::compareTo);

        while (min.isPresent() && (minMax == null || compareStrategy.compareTo(minMax, min.get()) >= 0)) {
            for (ICyclicBuffer cyclicBuffer : tmpCol) {
                while (cyclicBuffer.getSize() != 0 && min.get().equals(cyclicBuffer.getFirst())) {
                    cyclicBuffer.pull();
                    out.println(min.get());
                }
            }

            tmpCol.removeIf(p -> p.getSize() == 0);
            min = tmpCol.stream().map(ICyclicBuffer::getFirst).min(compareStrategy::compareTo);
        }

    }
}
