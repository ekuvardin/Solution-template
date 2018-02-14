package main.counters;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicReference;

/*
    Source is got from http://cr.openjdk.java.net/~plevart/misc/LeftRight/TLChainEEW.java
 */
public class TLChainEEW {

    private final AtomicReference<Entry> last = new AtomicReference<>();
    private final ThreadLocal<Entry> curValue = ThreadLocal.withInitial(() -> {
        Entry currentValue = new Entry();
        Entry curLast;
        do {
            curLast = last.get();
            currentValue.prev = curLast;
        } while (!last.compareAndSet(curLast, currentValue));
        return currentValue;
    });


    private static final class Entry extends WeakReference<Thread> {

        Entry prev;
        volatile boolean used;

        Entry() {
            super(Thread.currentThread());
        }
    }

    public void enter() {
        curValue.get().used = true;
    }

    public void exit() {
        curValue.get().used = false;
    }

    public void waitEmpty() {
        retry:
        while (true) {
            // remember 1st
            Entry f = last.get();
            Entry d = null;
            for (Entry e = f; e != null; e = e.prev) {
                if (e.get() == null) {
                    // entry is cleared, thread is gone -> expunge
                    if (d == null) {
                        if (last.compareAndSet(f, e.prev)) {
                            f = e.prev;
                        } else {
                            // first changed concurrently -> retry loop
                            continue retry;
                        }
                    } else {
                        d.prev = e.prev;
                    }
                } else {
                    while (e.used) {
                        Thread.yield();
                    }
                    d = e;
                }
            }
            break;
        }
    }
}
