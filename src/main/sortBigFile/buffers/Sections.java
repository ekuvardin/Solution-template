package main.sortBigFile.buffers;

import main.sortBigFile.buffers.CyclicBufferHolder;
import main.sortBigFile.buffers.ICyclicBuffer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Sections {

    protected final Map<Integer, ICyclicBuffer> usedSections;

    private CyclicBufferHolder cyclicBufferHolder;

    public Sections(CyclicBufferHolder cyclicBufferHolder, int size) {
        this.cyclicBufferHolder = cyclicBufferHolder;
        usedSections = new ConcurrentHashMap<>(size);

        Iterator<ICyclicBuffer> cl = cyclicBufferHolder.getCyclicBuffer(size).iterator();
        for (int i = 0; i < size; i++) {
            usedSections.put(i, cl.next());
        }
    }

    public void tryFreeMemory() {
        for (Map.Entry<Integer, ICyclicBuffer> val : usedSections.entrySet()) {
            free(val.getKey());
        }
    }

    protected ICyclicBuffer getBufferAt(Integer index) {
        return usedSections.get(index);
    }

    protected void free(Integer index) {
        ICyclicBuffer val = getBufferAt(index);
        if (val != null && val.getSize() == 0) {
            usedSections.remove(index);
            cyclicBufferHolder.putCyclicBuffer(val);
        }
    }

    public Map<Integer, ICyclicBuffer> getUsedSections() {
        return Collections.unmodifiableMap(usedSections);
    }

    public Collection<ICyclicBuffer> getBuffers() {
        return Collections.unmodifiableCollection(usedSections.values());
    }
}
