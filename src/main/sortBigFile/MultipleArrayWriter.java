package main.sortBigFile;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;

public class MultipleArrayWriter {

    private Sections sections;

    public MultipleArrayWriter(Sections sections) {
        this.sections = sections;
    }

    public void fillBuffer() {
        List<Scanner> deleted = new ArrayList<>();
        for (Map.Entry<Scanner, CyclicBufferHolder.CyclicBuffer> entry : sections.getUsedSections().entrySet()) {
            Scanner scanner = entry.getKey();
            CyclicBufferHolder.CyclicBuffer buffer = entry.getValue();
            while (scanner.hasNextInt() && buffer.getSize() > 0) {
                buffer.put(scanner.nextInt());
            }

            if (!scanner.hasNextInt()) {
                deleted.add(scanner);
            }
        }

        for (Scanner i : deleted) {
            sections.free(i);
            i.close();
        }
    }
}
