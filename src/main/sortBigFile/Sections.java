package main.sortBigFile;

import java.util.*;

public class Sections {

    private final Map<Scanner, CyclicBufferHolder.CyclicBuffer> usedSections;
    private CyclicBufferHolder cyclicBufferHolder;

    public Sections(List<Scanner> scanners, CyclicBufferHolder cyclicBufferHolder){
        usedSections = new HashMap<>(scanners.size());
        Iterator<CyclicBufferHolder.CyclicBuffer> cl = cyclicBufferHolder.getCyclicBuffer(scanners.size()).iterator();
        for (Scanner scanner : scanners) {
            usedSections.put(scanner, cl.next());
        }
    }

    public void addSection(Scanner scanner){
        if(usedSections.containsKey(scanner)){
            return;
        }

        usedSections.put(scanner, cyclicBufferHolder.getCyclicBuffer(1).get(0));
    }

    public void free(Scanner scanner){
        CyclicBufferHolder.CyclicBuffer buffer = usedSections.remove(scanner);
        cyclicBufferHolder.putCyclicBuffer(buffer);
    }

    public Map<Scanner, CyclicBufferHolder.CyclicBuffer> getUsedSections() {
        return usedSections;
    }

    public Collection<CyclicBufferHolder.CyclicBuffer> getBuffers(){
        return usedSections.values();
    }
}
