package main.sortBigFile.readers;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FileNamesHolder {

    private Queue<String> files = new ArrayDeque<>();
    private volatile int size = 0;
    private AtomicInteger lastGen = new AtomicInteger();

    public List<String> get(int count) {
        while (true) {
            if (size >= count) {
                synchronized (this) {
                    if (size >= count) {
                        List<String> res = new ArrayList<>(count);
                        for (int i = 0; i < count; i++) {
                            res.add(files.remove());
                        }
                        size = size - count;
                        return res;
                    }
                }
            }
        }
    }

    public void pull(String inp) {
        synchronized (this) {
            files.add(inp);
            size++;
        }
    }

    public String getNewUniqueName(String name){
        return name + this.lastGen.incrementAndGet();
    }

    public int getSize() {
        return size;
    }
}
