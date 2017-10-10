package main.executor;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Links all working threads in chain.
 */
public class ChainingThread {

    private final AtomicReference<Entry> tail = new AtomicReference<>();

    /**
     * Class-wrapper for manually add Thread to chain due to MainRunner may start later
     * then we immediately shutdown Executor after creation
     */
    static class Worker extends Thread {

        private final ChainingThread.Entry localEntry;

        public Worker(Runnable target, ChainingThread chainingThread) {
            super(target);

            localEntry = new Entry(this);
            Entry lastNode;

            do {
                lastNode = chainingThread.tail.get();
                localEntry.prev = lastNode;
            }
            while (!chainingThread.tail.compareAndSet(lastNode, localEntry));

            localEntry.inUsed = true;
        }

        @Override
        public synchronized void run() {
            try {
                super.run();
            } finally {
                localEntry.inUsed = false;
            }
        }
    }

    /**
     * If threads are cleaned by GC then we can simply
     * skip this reference
     */
    static class Entry extends WeakReference<Worker> {

        Entry prev = null;
        volatile boolean inUsed = false;

        Entry(Worker thread) {
            super(thread);
        }
    }

    /**
     * Not threadSafe
     * Check is current tail is empty
     *
     * @return is current tail is empty
     */
    public boolean isEmpty() {
        for (Entry curNode = tail.get(); curNode != null; curNode = curNode.prev) {
            if (curNode.get() != null && curNode.inUsed) {
                return false;
            }
        }

        return true;
    }

    /**
     * Wait while all threads stop executing.
     * Using this method we make assumption that new thread aren't allowed to add to existing chain
     * <p>
     * Note: we use System.currentTimeMillis() which behaviour dramatically different in OS implementation(but we don't care on it)
     *
     * @param seconds how much time wait in seconds
     */
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

    /**
     * Check if exists working threads
     *
     * @return Check if exists working threads
     */
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
