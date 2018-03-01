package sortBigFile.buffers;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class linking file readers and section where we could write
 *
 * @param <T> type of sorting elements
 */
public class SectionWriters<T> extends Sections<T> {

    private final Map<Integer, Scanner> usedScanners;

    public SectionWriters(CyclicBufferHolder<T> cyclicBufferHolder, List<Scanner> lst) {
        super(cyclicBufferHolder, lst.size());

        int size = lst.size();

        usedScanners = new ConcurrentHashMap<>(size);

        Iterator<Scanner> cl = lst.iterator();
        for (int i = 0; i < size; i++) {
            usedScanners.put(i, cl.next());
        }
    }

    /**
     * Return array readers
     *
     * @return array readers
     */
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

    @Override
    public void close() {
        usedScanners.values().forEach(Scanner::close);
        usedScanners.clear();
        super.close();
    }
}
