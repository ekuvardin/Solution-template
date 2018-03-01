package sortBigFile.writers;

import sortBigFile.buffers.ICyclicBuffer;
import sortBigFile.buffers.SectionWriters;

import java.util.*;

/**
 * Scan files and write result to corresponding buffer
 *
 * @param <T> type of reading elements
 */
public class ArrayWriter<T> implements IArrayWriter {

    private final SectionWriters<T> sectionWriters;
    private final IValueScanner<T> valueScanner;

    public ArrayWriter(SectionWriters<T> sectionWriters, IValueScanner<T> valueScanner) {
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
