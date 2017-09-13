package main.sortBigFile.writers;

import main.sortBigFile.buffers.ICyclicBuffer;
import main.sortBigFile.buffers.SectionWriters;

import java.util.*;

public class ArrayWriterImpl implements IArrayWriter {

    private SectionWriters sectionWriters;

    public ArrayWriterImpl(SectionWriters sectionWriters) {
        this.sectionWriters = sectionWriters;
    }

    @Override
    public void fillBuffer() {
        for (Map.Entry<Integer, Scanner> entry : sectionWriters.getUsedScanners().entrySet()) {
            Scanner scanner = entry.getValue();

            ICyclicBuffer buffer = sectionWriters.getUsedSections().get(entry.getKey());

            while (scanner.hasNextInt() && (buffer.getCapacity() - buffer.getSize() > 0)) {
                buffer.put(scanner.nextInt());
            }
        }
    }

}
