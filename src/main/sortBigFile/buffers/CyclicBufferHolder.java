package main.sortBigFile.buffers;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class CyclicBufferHolder {

    final List<ICyclicBuffer> list;
    private volatile int size;

    public CyclicBufferHolder(Integer[] array, int chunks) {
        list = new ArrayList<>(chunks);

        int chunk = array.length / chunks;
        for (int i = 0, start = 0; i < chunks; i++) {
            final int end = (i == chunks - 1) ? array.length : start + chunk;
            list.add(new CyclicBuffer(array, start, end));
            start = end;
        }

        size = chunks;
    }

    public List<ICyclicBuffer> getCyclicBuffer(int count) {
        while(true){
            if(size >= count) {
                synchronized (this) {
                    if(size >= count) {
                        List<ICyclicBuffer> res = new ArrayList<>(list.subList(0,count));
                        list.removeAll(res);
                        size = size - count;
                        return res;
                    }
                }
            }
        }
    }

    public synchronized void putCyclicBuffer(List<ICyclicBuffer> buffers){
            list.addAll(buffers);
            size = size + buffers.size();
    }

    public synchronized void putCyclicBuffer(ICyclicBuffer buffer){
            list.add(buffer);
            size++;
    }

    static class CyclicBuffer implements ICyclicBuffer {
        final Integer array[];
        final int startPointer;
        final int endPointer;

        int head = -1;
        int tail = -1;
        int curSize = 0;

        private CyclicBuffer(Integer[] array, int startPointer, int endPointer) {
            this.array = array;
            this.startPointer = startPointer;
            this.endPointer = endPointer;
        }

        @Override
        public Integer getFirst() {
            if (curSize == 0) {
                throw new NoSuchElementException();
            }
            return array[head];
        }

        @Override
        public Integer getLast() {
            if (curSize == 0) {
                throw new NoSuchElementException();
            }
            return array[tail];
        }

        @Override
        public int getSize() {
            return curSize;
        }

        @Override
        public int getCapacity() {
            return endPointer - startPointer;
        }

        @Override
        public void put(Integer value) {
            if (getSize() == endPointer - startPointer) {
                throw new RuntimeException();
            }

            curSize++;

            if (tail == -1) {
                head = tail = startPointer;
            } else if (tail == endPointer - 1) {
                tail = startPointer;
            } else {
                tail++;
            }

            array[tail] = value;
        }

        @Override
        public Integer pull() {
            Integer value = getFirst();

            if (--curSize == 0) {
                head = tail = -1;
            } else if (head == endPointer - 1) {
                head = startPointer;
            } else {
                head++;
            }

            return value;
        }
    }
}
