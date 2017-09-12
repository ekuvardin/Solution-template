package main.sortBigFile;

import java.io.PrintWriter;
import java.util.function.Function;

public class MergeArrayToFiles {

    private Sections sections;

    public MergeArrayToFiles(Sections sections) {
        this.sections = sections;
    }

    public void mergeTillEnd(PrintWriter out) {
        Integer min = findMin(v -> v.getFirst());
        while (true) {
            for (CyclicBufferHolder.CyclicBuffer cyclicBuffer : sections.getUsedSections().values()) {
                while (cyclicBuffer.getSize()!=0 && min.equals(cyclicBuffer.getFirst())) {
                    cyclicBuffer.pull();
                    out.println(min);
                }
            }

            min = findMin(v -> v.getFirst());
        }
    }

    public void merge(PrintWriter out) {
        Integer minMax = findMin(v -> v.getLast());

        Integer min = findMin(v -> v.getFirst());
        while (minMax.compareTo(min) >= 0) {
            for (CyclicBufferHolder.CyclicBuffer cyclicBuffer : sections.getBuffers()) {
                while (cyclicBuffer.getSize()!=0 && min.equals(cyclicBuffer.getFirst())) {
                    cyclicBuffer.pull();
                    out.println(min);
                }
            }

            min = findMin(v -> v.getFirst());
        }
    }

    private Integer findMin(Function<CyclicBufferHolder.CyclicBuffer, Integer> func) {
        Integer min = Integer.MAX_VALUE;
        for (CyclicBufferHolder.CyclicBuffer cyclicBuffer : sections.getBuffers()) {
            if(cyclicBuffer.getSize()!=0) {
                Integer val = func.apply(cyclicBuffer);
                min = val.compareTo(min) < 0 ? val : min;
            }
        }

        return min;
    }
}
