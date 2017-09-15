package main.sortBigFile.buffers;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Sections<T extends Comparable<T>> implements AutoCloseable {

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

    public void tryFreeMemory() {
        for (Map.Entry<Integer, ICyclicBuffer<T>> val : usedSections.entrySet()) {
            free(val.getKey());
        }
    }

    protected ICyclicBuffer<T> getBufferAt(Integer index) {
        return usedSections.get(index);
    }

    protected void free(Integer index) {
        ICyclicBuffer<T> val = getBufferAt(index);
        if (val != null && val.getSize() == 0) {
            usedSections.remove(index);
            cyclicBufferHolder.putCyclicBuffer(val);
        }
    }

    public Map<Integer, ICyclicBuffer<T>> getUsedSections() {
        return Collections.unmodifiableMap(usedSections);
    }

    public Collection<ICyclicBuffer<T>> getBuffers() {
        return Collections.unmodifiableCollection(usedSections.values());
    }

    @Override
    public void close() throws Exception {
        this.tryFreeMemory();
        cyclicBufferHolder.putCyclicBuffer(usedSections.values());
        usedSections.clear();
    }
}
