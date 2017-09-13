package main.sortBigFile.readers;

import main.sortBigFile.buffers.ICyclicBuffer;
import main.sortBigFile.buffers.Sections;

import java.io.PrintWriter;
import java.util.*;

public class ArrayReaderImpl implements IArrayReader {

    private Sections sections;

    public ArrayReaderImpl(Sections sections) {
        this.sections = sections;
    }

    @Override
    public void mergeTillEnd(PrintWriter out) {
        List<ICyclicBuffer> tmpCol = new ArrayList<>(sections.getBuffers());
        tmpCol.removeIf(p -> p.getSize() == 0);
        mergeTillStopKey(out, Optional.empty(), tmpCol);
    }

    @Override
    public void merge(PrintWriter out) {
        List<ICyclicBuffer> tmpCol = new ArrayList<>(sections.getBuffers());
        tmpCol.removeIf(p -> p.getSize() == 0);
        mergeTillStopKey(out, tmpCol.stream().map(v -> v.getLast()).min(Integer::compare), tmpCol);
    }

    private void mergeTillStopKey(PrintWriter out, Optional<Integer> minMax, List<ICyclicBuffer> tmpCol) {
        Optional<Integer> min = tmpCol.stream().map(v -> v.getFirst()).min(Integer::compare);

        while (min.isPresent() && comp(minMax, min)) {
            for (ICyclicBuffer cyclicBuffer : tmpCol) {
                while (cyclicBuffer.getSize() != 0 && min.get().equals(cyclicBuffer.getFirst())) {
                    cyclicBuffer.pull();
                    out.println(min.get());
                }
            }

            tmpCol.removeIf(p -> p.getSize() == 0);
            min = tmpCol.stream().map(v -> v.getFirst()).min(Integer::compare);
        }

    }

    public boolean comp(Optional<Integer> minMax, Optional<Integer> min) {
        if (minMax.isPresent()) {
            return minMax.get().compareTo(min.get()) >= 0;
        }

        return true;
    }

}
