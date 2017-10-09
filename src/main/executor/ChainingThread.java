package main.executor;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ChainingThread {

    private final AtomicReference<Entry> tail = new AtomicReference<>();

    private final ThreadLocal<Entry> localEntry = new ThreadLocal<Entry>() {

        @Override
        protected Entry initialValue() {
            Entry curNode = new Entry();
            Entry lastNode;

            do {
                lastNode = tail.get();
                curNode.prev = lastNode;
            }
            while (!tail.compareAndSet(lastNode, curNode));

            return curNode;
        }
    };


    static class Entry extends WeakReference<Thread> {

        Entry prev = null;
        volatile boolean inUsed = false;

        Entry() {
            super(Thread.currentThread());
        }
    }

    public void add() {
        localEntry.get().inUsed = true;
    }

    public void remove() {
        localEntry.get().inUsed = false;
    }

    public boolean isEmpty() {
        for (Entry curNode = tail.get(); curNode != null; curNode = curNode.prev) {
            if (curNode.get() != null && curNode.inUsed) {
                return false;
            }
        }

        return true;
    }

    public void waitEmpty(long seconds) {
        final long barrier = System.currentTimeMillis() + seconds * 1000;
        for (Entry curNode = tail.get(); curNode != null; curNode = curNode.prev) {
            if (curNode.get() != null) {
                while (curNode.inUsed && System.currentTimeMillis() < barrier) {
                    Thread.yield();
                }
            }
        }
    }

    public List<Thread> workingThreads() {
        List<Thread> res = new ArrayList<>();
        for (Entry curNode = tail.get(); curNode != null; curNode = curNode.prev) {
            if (curNode.get() != null && !curNode.inUsed) {
                res.add(curNode.get());
            }
        }

        return res;
    }
}
