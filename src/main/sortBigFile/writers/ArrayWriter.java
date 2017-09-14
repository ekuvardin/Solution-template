package main.sortBigFile.writers;

import main.sortBigFile.buffers.ICyclicBuffer;
import main.sortBigFile.buffers.SectionWriters;

import java.util.*;

public class ArrayWriter<T extends Comparable<T>> implements IArrayWriter {

    private SectionWriters sectionWriters;
    private IValueScanner<T> valueScanner;

    public ArrayWriter(SectionWriters sectionWriters, IValueScanner<T> valueScanner) {
        this.sectionWriters = sectionWriters;
        this.valueScanner = valueScanner;
    }

    @Override
    public void fillBuffer() {
        Map<Integer, Scanner> scanners = sectionWriters.getUsedScanners();

        for (Map.Entry<Integer, Scanner> entry : scanners.entrySet()) {
            Scanner scanner = entry.getValue();

            Map<Integer, ICyclicBuffer<T>> sections = sectionWriters.getUsedSections();
            ICyclicBuffer<T> buffer = sections.get(entry.getKey());

            while (valueScanner.hasNext(scanner) && (buffer.getCapacity() - buffer.getSize() > 0)) {
                buffer.put(valueScanner.nextValue(scanner));
            }
        }
    }

}
