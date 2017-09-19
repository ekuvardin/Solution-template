package main.sortBigFile.buffers;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class for holding cyclic buffers that are used by program
 *
 * @param <T> type of sorting elements
 */
public class Sections<T> implements AutoCloseable {

    protected final Map<Integer, ICyclicBuffer<T>> usedSections;

    private CyclicBufferHolder<T> cyclicBufferHolder;

    public Sections(CyclicBufferHolder<T> cyclicBufferHolder, int size) {
        this.cyclicBufferHolder = cyclicBufferHolder;
        usedSections = new ConcurrentHashMap<>(size);

        Iterator<ICyclicBuffer<T>> cl = cyclicBufferHolder.getCyclicBuffer(size).iterator();
        for (int i = 0; i < size; i++) {
            usedSections.put(i, cl.next());
        }
    }

    /**
     * free occupied memory
     */
    public void tryFreeMemory() {
        for (Map.Entry<Integer, ICyclicBuffer<T>> val : usedSections.entrySet()) {
            free(val.getKey());
        }
    }

    /**
     * Get array by identified index
     *
     * @param index array index
     * @return array by index or null
     */
    protected ICyclicBuffer<T> getBufferAt(Integer index) {
        return usedSections.get(index);
    }

    /**
     * Free memory of single element identified by index
     *
     * @param index array index
     */
    protected void free(Integer index) {
        ICyclicBuffer<T> val = getBufferAt(index);
        if (val != null && val.getSize() == 0) {
            usedSections.remove(index);
            cyclicBufferHolder.putCyclicBuffer(val);
        }
    }

    /**
     * Get occupied Sections with identifiers
     *
     * @return occupied Sections with identifiers
     */
    public Map<Integer, ICyclicBuffer<T>> getUsedSections() {
        return Collections.unmodifiableMap(usedSections);
    }

    /**
     * Get occupied Sections
     *
     * @return occupied Sections
     */
    public Collection<ICyclicBuffer<T>> getBuffers() {
        return Collections.unmodifiableCollection(usedSections.values());
    }

    @Override
    public void close() {
        usedSections.values().forEach(ICyclicBuffer::reset);
        cyclicBufferHolder.putCyclicBuffer(usedSections.values());
        usedSections.clear();
    }
}
