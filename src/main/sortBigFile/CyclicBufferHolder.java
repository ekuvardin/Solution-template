package main.sortBigFile;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class CyclicBufferHolder {

    final List<CyclicBuffer> list;
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

    public List<CyclicBuffer> getCyclicBuffer(int count) {
        while(true){
            if(size >= count) {
                synchronized (list) {
                    if(size >= count) {
                        count--;
                        return list.subList(0,count-1);
                    }
                }
            }
        }
    }

    public void putCyclicBuffer(List<CyclicBuffer> buffers){
        synchronized(list){
            list.addAll(buffers);
            size = size + buffers.size();
        }
    }

    public void putCyclicBuffer(CyclicBuffer buffer){
        synchronized(list){
            list.add(buffer);
            size = size++;
        }
    }

    public void fillBuffer(Integer value, int chunk) {
        list.get(chunk).put(value);
    }

    public int getRemainingSize(int chunk) {
        CyclicBuffer cyclicBuffer = list.get(chunk);
        return cyclicBuffer.getCapacity() - cyclicBuffer.getSize();
    }

    public int getSize() {
        return list.stream().mapToInt(p -> p.getSize()).sum();
    }

    static class CyclicBuffer {
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

        public Integer getFirst() {
            if (curSize == 0) {
                throw new NoSuchElementException();
            }
            return array[head];
        }

        public Integer getLast() {
            if (curSize == 0) {
                throw new NoSuchElementException();
            }
            return array[tail];
        }

        public int getSize() {
            return curSize;
        }

        public int getCapacity() {
            return endPointer - startPointer;
        }

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
