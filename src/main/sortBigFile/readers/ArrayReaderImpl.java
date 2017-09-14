package main.sortBigFile.readers;

import main.sortBigFile.buffers.ICyclicBuffer;
import main.sortBigFile.buffers.Sections;

import java.io.PrintWriter;
import java.util.*;

public class ArrayReaderImpl<T extends Comparable<T>> implements IArrayReader {

    private Sections<T> sections;

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

        mergeTillStopKey(out, tmpCol.stream().map(ICyclicBuffer::getLast).min(T::compareTo), tmpCol);
    }

    private void mergeTillStopKey(PrintWriter out, Optional<T> minMax, List<ICyclicBuffer<T>> tmpCol) {
        Optional<T> min = tmpCol.stream().map(ICyclicBuffer::getFirst).min(T::compareTo);

        while (min.isPresent() && (!minMax.isPresent() || minMax.get().compareTo(min.get()) >= 0)) {
            for (ICyclicBuffer cyclicBuffer : tmpCol) {
                while (cyclicBuffer.getSize() != 0 && min.get().equals(cyclicBuffer.getFirst())) {
                    cyclicBuffer.pull();
                    out.println(min.get());
                }
            }

            tmpCol.removeIf(p -> p.getSize() == 0);
            min = tmpCol.stream().map(ICyclicBuffer::getFirst).min(T::compareTo);
        }

    }
}
