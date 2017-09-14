package main.sortBigFile.buffers;

import java.awt.event.ComponentAdapter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SectionWriters extends Sections {

    private final Map<Integer, Scanner> usedScanners;

    public SectionWriters(CyclicBufferHolder cyclicBufferHolder, List<Scanner> lst) {
        super(cyclicBufferHolder, lst.size());

        int size = lst.size();

        usedScanners = new ConcurrentHashMap<>(size);

        Iterator<Scanner> cl = lst.iterator();
        for (int i = 0; i < size; i++) {
            usedScanners.put(i, cl.next());
        }
    }

    public Map<Integer, Scanner> getUsedScanners() {
        return usedScanners;
    }

    @Override
    public void tryFreeMemory() {
        for (Map.Entry<Integer, Scanner> entry : usedScanners.entrySet()) {
            free(entry.getKey());
        }

        super.tryFreeMemory();
    }

    @Override
    protected void free(Integer index) {
        Scanner scanner = usedScanners.get(index);

        if (scanner == null) {
            super.free(index);
        } else if (!scanner.hasNextInt()) {
            usedScanners.remove(index);
            scanner.close();
            super.free(index);
        }
    }
}
