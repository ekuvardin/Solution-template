package main.sortBigFile.readers;

import main.sortBigFile.buffers.ICyclicBuffer;
import main.sortBigFile.buffers.Sections;

import java.awt.event.ComponentAdapter;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.Function;

public class ArrayReaderImpl<T extends Comparable<T>> implements IArrayReader {

    private Sections sections;

    public ArrayReaderImpl(Sections<T> sections) {
        this.sections = sections;
    }

    @Override
    public void mergeTillEmpty(PrintWriter out) {
        List<ICyclicBuffer<T>> tmpCol = new ArrayList<>(sections.getBuffers());
        tmpCol.removeIf(p -> p.getSize() == 0);

        mergeTillStopKey(out, Optional.empty(), tmpCol);
    }

    @Override
    public void merge(PrintWriter out) {
        List<ICyclicBuffer<T>> tmpCol = new ArrayList<>(sections.getBuffers());
        tmpCol.removeIf(p -> p.getSize() == 0);

        mergeTillStopKey(out, tmpCol.stream().map(vv -> vv.getLast()).min(T::compareTo), tmpCol);
    }

    private void merge() {

    }

    private void mergeTillStopKey(PrintWriter out, Optional<T> minMax, List<ICyclicBuffer<T>> tmpCol) {
        Optional<T> min = tmpCol.stream().map(v -> v.getFirst()).min(T::compareTo);

        while (min.isPresent() && comp(minMax, min)) {
            for (ICyclicBuffer cyclicBuffer : tmpCol) {
                while (cyclicBuffer.getSize() != 0 && min.get().equals(cyclicBuffer.getFirst())) {
                    cyclicBuffer.pull();
                    out.println(min.get());
                }
            }

            tmpCol.removeIf(p -> p.getSize() == 0);
            min = tmpCol.stream().map(v -> v.getFirst()).min(T::compareTo);
        }

    }

    public boolean comp(Optional<T> minMax, Optional<T> min) {
        if (minMax.isPresent()) {
            return minMax.get().compareTo(min.get()) >= 0;
        }

        return true;
    }

}
